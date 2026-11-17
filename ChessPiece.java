import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChessPiece {
    private String name = "";      // "king", "queen", "rook", "bishop", "knight", "pawn"
    private boolean isWhite;
    private int positionX, positionY; // board square (0-7), not pixels
    private final List<Point> legalMoves = new ArrayList<>(); 


    // One image per piece/color, shared by every piece of that kind (e.g. all 8 white pawns)
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();

    public ChessPiece(String name, boolean isWhite, int positionX, int positionY){
        this.name = name.toLowerCase();
        this.isWhite = isWhite;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public void drawPiece(Graphics2D g2d, int blockSize){
        BufferedImage img = getImage(name, isWhite, blockSize);
        // +1 skips the gray border column/row around the board
        int x = (positionX + 1) * blockSize;
        int y = (positionY + 1) * blockSize;
        g2d.drawImage(img, x, y, null);
    }

    // Returns the cached image, creating it the first time it's needed
    private static BufferedImage getImage(String name, boolean isWhite, int size){
        String key = name + (isWhite ? "_white_" : "_black_") + size;
        return imageCache.computeIfAbsent(key, k -> bufferPiece(name, isWhite, size));
    }

    // Draws a piece once into a transparent BufferedImage
    private static BufferedImage bufferPiece(String name, boolean isWhite, int size){
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB); // ARGB = transparent background
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Turn the chess symbol into a shape so we can fill and outline it
        String glyph = glyphFor(name);
        Font font = pickFont(glyph, size * 0.8f);
        FontRenderContext frc = g2d.getFontRenderContext();
        Shape outline = new TextLayout(glyph, font, frc).getOutline(null);

        // Center the shape in the square
        Rectangle2D b = outline.getBounds2D();
        double dx = (size - b.getWidth()) / 2 - b.getX();
        double dy = (size - b.getHeight()) / 2 - b.getY();
        outline = AffineTransform.getTranslateInstance(dx, dy).createTransformedShape(outline);

        Color fill = isWhite ? Color.WHITE : Color.BLACK;
        Color line = isWhite ? Color.BLACK : Color.WHITE;
        float stroke = Math.max(1.5f, size / 30f);

        // 1. A solid "plate" in the opposite color, shaped like the piece but a bit bigger.
        //    It gives the piece an outline and fills the gaps between its stripes,
        //    so the piece is readable on both light and dark squares.
        Area plate = new Area(outline);
        plate.add(new Area(new BasicStroke(stroke * 3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                .createStrokedShape(outline)));
        g2d.setColor(line);
        g2d.fill(solidSilhouette(plate));

        // 2. The piece itself on top; its gaps let the plate show through as detail lines
        g2d.setColor(fill);
        g2d.fill(outline);

        g2d.dispose();
        return img;
    }

    // Merges every closed piece of the shape together, which fills in the inner holes
    private static Area solidSilhouette(Shape shape){
        Area solid = new Area();
        Path2D.Double part = new Path2D.Double();
        double[] c = new double[6];
        for (PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next()) {
            switch (it.currentSegment(c)) {
                case PathIterator.SEG_MOVETO:
                    if (part.getCurrentPoint() != null) { // new contour started without closing the last one
                        part.closePath();
                        solid.add(new Area(part));
                        part = new Path2D.Double();
                    }
                    part.moveTo(c[0], c[1]);
                    break;
                case PathIterator.SEG_LINETO:  part.lineTo(c[0], c[1]); break;
                case PathIterator.SEG_QUADTO:  part.quadTo(c[0], c[1], c[2], c[3]); break;
                case PathIterator.SEG_CUBICTO: part.curveTo(c[0], c[1], c[2], c[3], c[4], c[5]); break;
                case PathIterator.SEG_CLOSE:
                    part.closePath();
                    solid.add(new Area(part));
                    part = new Path2D.Double();
                    break;
            }
        }
        return solid;
    }

    // Filled chess symbols; the fill color decides white vs black
    private static String glyphFor(String name){
        switch (name) {
            case "king":   return "♚";
            case "queen":  return "♛";
            case "rook":   return "♜";
            case "bishop": return "♝";
            case "knight": return "♞";
            case "pawn":   return "♟";
            default: throw new IllegalArgumentException("Unknown piece: " + name);
        }
    }

    // Use the first installed font that has chess symbols
    private static Font pickFont(String glyph, float size){
        String[] candidates = {"Segoe UI Symbol", "DejaVu Sans", "Arial Unicode MS", "Apple Symbols", Font.SERIF};
        for (String fontName : candidates) {
            Font f = new Font(fontName, Font.PLAIN, 1);
            if (f.canDisplayUpTo(glyph) == -1) return f.deriveFont(size);
        }
        return new Font(Font.SERIF, Font.PLAIN, 1).deriveFont(size);
    }

    // private static void setLegalMoves(String name){
    //     switch (name){
    //         case "king"
    //     }
    // }

    public String getName(){ return name; }
    public boolean isWhite(){ return isWhite; }
    public int getPositionX(){ return positionX; }
    public int getPositionY(){ return positionY; }

    public void setPosition(int positionX, int positionY){
        this.positionX = positionX;
        this.positionY = positionY;
    }
}
