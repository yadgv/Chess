import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
public class Board extends JPanel {

    private final int blockSize = 80; // Size of each block in pixels
    private final int rows = 10;
    private final int cols = 10;
    private final BufferedImage board = new BufferedImage(blockSize * cols, blockSize * rows, BufferedImage.TYPE_INT_RGB);
    private final List<ChessPiece> pieces = new ArrayList<>();
    private boolean firstClick = true;
    private ChessPiece clickedPiece;
    private ChessPiece pieceCaptured;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chess");
            Board panel = new Board();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }

    public Board() {
        setPreferredSize(new Dimension(blockSize * cols, blockSize * rows)); //Setting the size of the window
        bufferBoard(); // Load the board into a buffered image to improve performance
        setupPieces(); // Place every piece in its starting square
        setFocusable(true); //Focuses on window to listen for key events

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int clickedY = e.getY()/blockSize - 1;
                int clickedX = e.getX()/blockSize - 1;
                if (firstClick){
                    for (ChessPiece piece : pieces){
                        if (piece.getPositionX() == clickedX && piece.getPositionY() == clickedY) {
                            clickedPiece = piece;
                            firstClick = false;
                        } 
                    }
                 } else if (!firstClick && clickedPiece != null){
                    if (pieceCaptured == null){
                        for (ChessPiece piece : pieces){
                            if (piece.getPositionX() == clickedX && piece.getPositionY() == clickedY) {
                                pieceCaptured = piece;
                            }
                        } 
                    }
                    if (pieceCaptured != clickedPiece) pieces.remove(pieceCaptured);
                    clickedPiece.setPosition(clickedX, clickedY);
                    firstClick = true;
                    clickedPiece = null;
                    pieceCaptured = null;
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        drawBoard(g2d);
        for (ChessPiece piece : pieces) piece.drawPiece(g2d, blockSize);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
    }

    private void bufferBoard(){
        Graphics2D g2d = board.createGraphics();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * blockSize, y = row * blockSize;
                int half = blockSize / 2;
                boolean right = col == cols - 1, left = col == 0;
                boolean top = row == 0, bottom = row == rows - 1;

                int rx = (left) ? x+half : x;
                int ry = (top) ? y+half : y;
                int rw = (right || left) ? half : blockSize;
                int rh = (top || bottom) ? half : blockSize;

                if ((right || left || top || bottom)) g2d.setColor(Color.GRAY);
                else if ((row + col) % 2 == 0) g2d.setColor(Color.BLACK);
                else g2d.setColor(Color.WHITE);
                
                g2d.fillRect(rx, ry, rw, rh);
            }
        }
        g2d.dispose();
    }

    private void setupPieces(){
        String[] backRow = {"rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"};
        for (int col = 0; col < 8; col++) {
            pieces.add(new ChessPiece(backRow[col], false, col, 0)); // black back row
            pieces.add(new ChessPiece("pawn", false, col, 1));       // black pawns
            pieces.add(new ChessPiece("pawn", true, col, 6));        // white pawns
            pieces.add(new ChessPiece(backRow[col], true, col, 7));  // white back row
        }
    }

    private void drawBoard(Graphics2D g2d) {
        g2d.drawImage(board, 0, 0, null);
    }

}