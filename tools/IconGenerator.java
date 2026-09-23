import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * The single source of truth for the F.L.A.M.E.S mark: an ember tile carrying
 * a cream heart under a gold flame-drop. Pure Java2D, runs headless, zero
 * dependencies. Regenerate with:
 *
 *   javac -d mk tools/IconGenerator.java
 *   java -Djava.awt.headless=true -cp mk IconGenerator src/main/resources/assets/icon
 *
 * The PNGs in that directory are checked in; this file is their source.
 */
public final class IconGenerator {

    private static final Color EMBER_TOP = new Color(0xC9, 0x3A, 0x2E);
    private static final Color EMBER_BOTTOM = new Color(0x8E, 0x23, 0x1C);
    private static final Color CREAM = new Color(0xFF, 0xF8, 0xEC);
    private static final Color GOLD = new Color(0xE8, 0xB5, 0x4A);

    public static void main(String[] args) throws Exception {
        File out = new File(args.length > 0 ? args[0] : "icon");
        out.mkdirs();
        int[] sizes = {16, 32, 48, 128, 256};
        List<byte[]> pngs = new ArrayList<>();
        for (int size : sizes) {
            BufferedImage image = draw(size);
            File file = new File(out, "icon-" + size + ".png");
            ImageIO.write(image, "png", file);
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", bytes);
                pngs.add(bytes.toByteArray());
            }
            System.out.println(file.getName() + " opaque=" + opaquePercent(image) + "%");
        }
        writeIco(new File(out, "flames.ico"), sizes, pngs);
        System.out.println("flames.ico written");
    }

    /** Draws the mark at any pixel size; coordinates are authored at 256. */
    private static BufferedImage draw(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double s = size / 256.0;
        g.scale(s, s);

        g.setPaint(new GradientPaint(0, 0, EMBER_TOP, 0, 256, EMBER_BOTTOM));
        g.fill(new RoundRectangle2D.Double(0, 0, 256, 256, 56, 56));

        // Gold flame-drop above the heart.
        g.setPaint(GOLD);
        g.fillOval(110, 46, 36, 36);
        g.fillPolygon(new int[]{128, 110, 146}, new int[]{16, 54, 54}, 3);

        // Cream heart: two lobes plus a downward wedge.
        g.setPaint(CREAM);
        g.fillOval(68, 102, 64, 64);
        g.fillOval(124, 102, 64, 64);
        g.fillPolygon(new int[]{66, 190, 128}, new int[]{138, 138, 208}, 3);

        g.dispose();
        return image;
    }

    private static long opaquePercent(BufferedImage image) {
        long opaque = 0;
        for (int y = 0; y < image.getHeight(); y += 2) {
            for (int x = 0; x < image.getWidth(); x += 2) {
                if ((image.getRGB(x, y) >>> 24) > 128) {
                    opaque++;
                }
            }
        }
        long total = (long) (image.getWidth() + 1) / 2 * ((image.getHeight() + 1) / 2);
        return Math.round(100.0 * opaque / total);
    }

    /** Minimal PNG-compressed .ico writer (16/32/48/256). */
    private static void writeIco(File file, int[] sizes, List<byte[]> pngs) throws Exception {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
            writeShortLE(out, 0); // reserved
            writeShortLE(out, 1); // .ico
            writeShortLE(out, sizes.length);
            int offset = 6 + 16 * sizes.length;
            for (int i = 0; i < sizes.length; i++) {
                int size = sizes[i];
                out.write(size >= 256 ? 0 : size); // width (0 = 256)
                out.write(size >= 256 ? 0 : size); // height
                out.write(0); // palette
                out.write(0); // reserved
                writeShortLE(out, 1); // planes
                writeShortLE(out, 32); // bpp
                writeIntLE(out, pngs.get(i).length);
                writeIntLE(out, offset);
                offset += pngs.get(i).length;
            }
            for (byte[] png : pngs) {
                out.write(png);
            }
        }
    }

    private static void writeShortLE(DataOutputStream out, int v) throws Exception {
        out.write(v & 0xFF);
        out.write((v >>> 8) & 0xFF);
    }

    private static void writeIntLE(DataOutputStream out, int v) throws Exception {
        for (int i = 0; i < 4; i++) {
            out.write((v >>> (8 * i)) & 0xFF);
        }
    }
}
