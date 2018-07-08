import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;


public class Sudoku extends JFrame implements ActionListener, KeyListener {
	private static final long serialVersionUID = 1L;
	private final int BOARDSIZE = 9;
	private final String GAMESFOLDER = "games";
	private final String INPUTFILE = "input.csv";
	private final String SOLUTIONFILE = "sln.csv";
	private final SimpleDateFormat clock = new SimpleDateFormat("mm.ss.SSS");
	
	private int[][] input;
	private int[][] solution;
	private long startTime;
	
	private JTextField[][] board;
	private JTextField editingField;
	private JLabel clockLabel;
	private Timer fieldTimer;
	private Timer clockTimer;
	
	public Sudoku() {
		this.setLayout(new BorderLayout(0, 10));
		this.setSize(400, 400);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Sudoku");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		startGame();
		
		JPanel boardPanel = new JPanel();
		boardPanel.setLayout(new GridLayout(BOARDSIZE, BOARDSIZE));
		for (int i=0; i<BOARDSIZE; i++) {
			for (int j=0; j<BOARDSIZE; j++) {
				boardPanel.add(board[i][j]);
			}
		}
		
		JPanel topPanel = new JPanel();
		clockLabel = new JLabel();
		clockLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		Border border = clockLabel.getBorder();
		Border margin = new EmptyBorder(10,0,0,0);
		clockLabel.setBorder(new CompoundBorder(border, margin));
		topPanel.add(clockLabel);
		
		clockTimer = new Timer(500, this);
		clockTimer.setRepeats(true);
		startTime = System.currentTimeMillis();
		clockTimer.start();
		
		this.add(topPanel, BorderLayout.NORTH);
		this.add(boardPanel, BorderLayout.CENTER);
	}
	
	private void startGame() {
		pickGame();
		
		if (input == null || solution == null) {
			JOptionPane.showMessageDialog(this, "Error!");
			System.exit(1);
		}
		
		initBoard();
		editingField = null;
	}
	
	private void updateClock() {
        Date elapsed = new Date(System.currentTimeMillis() - startTime);
        clockLabel.setText(clock.format(elapsed));
    }
	
	private void initBoard() {
		board = new JTextField[BOARDSIZE][BOARDSIZE];
		for (int i=0; i<BOARDSIZE; i++) {
			for (int j=0; j<BOARDSIZE; j++) {
				String text = Integer.toString(input[i][j]);
				if (text.equals("0")) text = "";
				board[i][j] = new JTextField();
				board[i][j].setText(text);
				board[i][j].setHorizontalAlignment(JTextField.CENTER);
				if (input[i][j] == solution[i][j]) {
					board[i][j].setEditable(false);
				} else {
					board[i][j].addKeyListener(this);
				}
			}
		}
	}
	
	private void pickGame() {
		File gamesFolder = new File(GAMESFOLDER);
		String[] directories = gamesFolder.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		
		int pickedGame = -1;
		if (directories != null && directories.length > 0) {
			Random rand = new Random();
			rand.setSeed(System.currentTimeMillis());
			pickedGame = rand.nextInt(directories.length);
			
			StringBuilder gameFolder = new StringBuilder();
			gameFolder.append(GAMESFOLDER);
			gameFolder.append("\\");
			gameFolder.append(directories[pickedGame]);
			gameFolder.append("\\");
			
			StringBuilder inputGame = new StringBuilder();
			inputGame.append(gameFolder);
			inputGame.append(INPUTFILE);
			
			StringBuilder solutionGame = new StringBuilder();
			solutionGame.append(gameFolder);
			solutionGame.append(SOLUTIONFILE);
			
			input = readSudokuGame(inputGame.toString());
			solution = readSudokuGame(solutionGame.toString());
		}
	}
	
	private int[][] readSudokuGame(String csvFile) {
		BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        int[][] array = new int[BOARDSIZE][BOARDSIZE];
		
		try {
		    br = new BufferedReader(new FileReader(csvFile));
		    int currentLine = 0;
		    while ((line = br.readLine()) != null) {
		        String[] lineElements = line.split(cvsSplitBy);
		        if (lineElements != null && lineElements.length == BOARDSIZE) {
		        	for (int i=0; i<lineElements.length; i++)
		        		array[currentLine][i] = Integer.parseInt(lineElements[i]);
		        }
		        currentLine++;
		    }
		} catch (FileNotFoundException e) {
		    array = null;
		} catch (IOException e) {
			array = null;
		} catch (Exception e) {
			array = null;
		} finally {
		    if (br != null) {
		        try {
		            br.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
		}
		
		return array;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fieldTimer && editingField != null) {
			editingField.setText("");
			editingField.validate();
			
			return;
		}
		
		if (e.getSource() == clockTimer) {
			updateClock();
		}
	}
	
	private void invalidInput(JTextField forField) {
		editingField = forField;
		fieldTimer = new Timer(500, this);
		fieldTimer.setRepeats(false);
		fieldTimer.start();
	}
	
	private boolean checkGameOver() {
		for (int i=0; i<BOARDSIZE; i++)
			for (int j=0; j<BOARDSIZE; j++)
				if (input[i][j] != solution[i][j])
					return false;
		return true;
	}
	
	private void gameIsOver() {
		clockTimer.stop();
		JOptionPane.showMessageDialog(this, "You solved the sudoku in " + clockLabel.getText() + "!");
	}
	
	@Override
	@SuppressWarnings("static-access")
	public void keyPressed(KeyEvent e) {
		if (board != null) {
			for (int i=0; i<BOARDSIZE; i++) {
				for (int j=0; j<BOARDSIZE; j++) {
					if (e.getSource() == board[i][j]) {
						try {
							int boardIJ = Integer.parseInt(e.getKeyText(e.getKeyCode()));
						
							if (boardIJ > 9) {
								invalidInput(board[i][j]);
							} else {
								if (boardIJ == solution[i][j]) {
									input[i][j] = solution[i][j];
									board[i][j].setText(Integer.toString(boardIJ));
									board[i][j].setEditable(false);
									
									boolean gameOver = checkGameOver();
									if (gameOver) {
										gameIsOver();
									}
								} else {
									invalidInput(board[i][j]);
								}
							}
						} catch (Exception ex) {
							invalidInput(board[i][j]);
						}
						
						return;
					}
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}	
	
	public static void main(String[] args) {
		new Sudoku().setVisible(true);
	}
}
