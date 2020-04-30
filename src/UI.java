import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import java.awt.Color;
import javax.swing.border.LineBorder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;


enum PaintMode {
	Pixel, Area, rec, oval
};

public class UI extends JFrame {
	private JTextField msgField;
	private JTextArea chatArea;
	private JPanel pnlColorPicker;
	private JPanel paintPanel;
	private JToggleButton tglPen;
	private JToggleButton tglBucket;
	private JToggleButton tglRD;
	private JToggleButton tglrec;
	private JToggleButton tgloval;
	private JToggleButton tglSave;
	private static UI instance;
	private int selectedColor = -543230; // golden
	String msg;
	String str = "";
	int[][] data = new int[50][50]; // pixel color data array
	int blockSize = 16;
	int[] change = new int[10];
	PaintMode paintMode = PaintMode.Pixel;
	int shapex, shapey, shapex2, shapey2;
	boolean sent = false;

	/**
	 * get the instance of UI. Singleton design pattern.
	 * 
	 * @return
	 */
	public static UI getInstance() {
		if (instance == null)
			instance = new UI();

		return instance;
	}

	public void Rectangle() {
		if (shapex2 < shapex) {
			int tmp = shapex;
			shapex = shapex2;
			shapex2 = tmp;
		}
		if (shapey2 < shapey) {
			int tmp = shapey;
			shapey = shapey2;
			shapey2 = tmp;
		}
		for (int i = shapex; i <= shapex2; i++) {
			for (int j = shapey; j <= shapey2; j++) {
				data[i][shapey] = selectedColor;
				data[shapex][j] = selectedColor;
				data[i][shapey2] = selectedColor;
				data[shapex2][j] = selectedColor;
			}
		}

		paintPanel.repaint();
	}

	public void Oval() {
		if (shapex2 < shapex) {
			int tmp = shapex;
			shapex = shapex2;
			shapex2 = tmp;
		}
		if (shapey2 < shapey) {
			int tmp = shapey;
			shapey = shapey2;
			shapey2 = tmp;
		}
		double c = Math.sqrt((shapex2 - shapex) * (shapex2 - shapex) - (shapey2 - shapey) * (shapey2 - shapey));
		// x2 = x1;
		int x1 = (shapex2 - shapex) / 2 - (int) c, x2 = (shapex2 - shapex) / 2 + 1 * (int) c,
				y = (shapey2 - shapey) / 2;
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				if (Math.sqrt((j - x1) * (j - x1) + (i - y) * (i - y))
						+ Math.sqrt((j - x2) * (j - x2) + (i - y) * (i - y)) - shapex2 + shapex <= 0.1)
					data[i][j] = selectedColor;
			}
		}

		paintPanel.repaint();
	}

	/**
	 * private constructor. To create an instance of UI, call UI.getInstance()
	 * instead.
	 */
	private UI() {
		setTitle("KidPaint");

		JPanel basePanel = new JPanel();
		getContentPane().add(basePanel, BorderLayout.CENTER);
		basePanel.setLayout(new BorderLayout(0, 0));

		paintPanel = new JPanel() {

			// refresh the paint panel
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				Graphics2D g2 = (Graphics2D) g; // Graphics2D provides the setRenderingHints method

				// enable anti-aliasing
				RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHints(rh);

				// clear the paint panel using black
				g2.setColor(Color.black);
				g2.fillRect(0, 0, this.getWidth(), this.getHeight());

				// draw and fill circles with the specific colors stored in the data array
				for (int x = 0; x < data.length; x++) {
					for (int y = 0; y < data[0].length; y++) {
						g2.setColor(new Color(data[x][y]));
						g2.fillArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
						g2.setColor(Color.darkGray);
						g2.drawArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
					}
				}
			}
		};

		paintPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if ((paintMode == PaintMode.rec || paintMode == PaintMode.oval) && e.getX() >= 0 && e.getY() >= 0) {
					shapex = e.getX() / blockSize;
					shapey = e.getY() / blockSize;
				}
			}

			// handle the mouse-up event of the paint panel
			@Override
			public void mouseReleased(MouseEvent e) {
				if (paintMode == PaintMode.Area && e.getX() >= 0 && e.getY() >= 0) {
					paintArea(e.getX() / blockSize, e.getY() / blockSize);

					change[1] = 2;
					change[2] = e.getX();
					change[3] = e.getY();
					change[4] = selectedColor;
					change[5] = shapex;
					change[6] = shapey;
					change[7] = shapex2;
					change[8] = shapey2;
				}
				if (paintMode == PaintMode.rec && e.getX() >= 0 && e.getY() >= 0) {
					shapex2 = e.getX() / blockSize;
					shapey2 = e.getY() / blockSize;
					Rectangle();
					change[1] = 4;
					change[2] = e.getX();
					change[3] = e.getY();
					change[4] = selectedColor;
					change[5] = shapex;
					change[6] = shapey;
					change[7] = shapex2;
					change[8] = shapey2;
				}
				if (paintMode == PaintMode.oval && e.getX() >= 0 && e.getY() >= 0) {
					shapex2 = e.getX() / blockSize;
					shapey2 = e.getY() / blockSize;
					Oval();
					change[1] = 5;
					change[2] = e.getX();
					change[3] = e.getY();
					change[4] = selectedColor;
					change[5] = shapex;
					change[6] = shapey;
					change[7] = shapex2;
					change[8] = shapey2;
				}
			}
		});

		paintPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (paintMode == PaintMode.Pixel && e.getX() >= 0 && e.getY() >= 0) {
					paintPixel(e.getX() / blockSize, e.getY() / blockSize);

					change[1] = 1;
					change[2] = e.getX();
					change[3] = e.getY();
					change[4] = selectedColor;
					change[5] = shapex;
					change[6] = shapey;
					change[7] = shapex2;
					change[8] = shapey2;
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}

		});

		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));

		JScrollPane scrollPaneLeft = new JScrollPane(paintPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		basePanel.add(scrollPaneLeft, BorderLayout.CENTER);

		JPanel toolPanel = new JPanel();
		basePanel.add(toolPanel, BorderLayout.NORTH);
		toolPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		pnlColorPicker = new JPanel();
		pnlColorPicker.setPreferredSize(new Dimension(24, 24));
		pnlColorPicker.setBackground(new Color(selectedColor));
		pnlColorPicker.setBorder(new LineBorder(new Color(0, 0, 0)));

		// show the color picker
		pnlColorPicker.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				ColorPicker picker = ColorPicker.getInstance(UI.instance);
				Point location = pnlColorPicker.getLocationOnScreen();
				location.y += pnlColorPicker.getHeight();
				picker.setLocation(location);
				picker.setVisible(true);
			}

		});

		toolPanel.add(pnlColorPicker);

		tglPen = new JToggleButton("Pen");
		tglPen.setSelected(true);
		toolPanel.add(tglPen);

		tglBucket = new JToggleButton("Bucket");
		toolPanel.add(tglBucket);

		tglRD = new JToggleButton("Redo");
		toolPanel.add(tglRD);
		tglrec = new JToggleButton("Rectangle");
		toolPanel.add(tglrec);
		
		tglSave = new JToggleButton("Save");
		toolPanel.add(tglSave);
		tgloval = new JToggleButton("Oval");
		toolPanel.add(tgloval);

		// change the paint mode to PIXEL mode
		tglPen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(true);
				tglBucket.setSelected(false);
				tglrec.setSelected(false);
				tgloval.setSelected(false);
				paintMode = PaintMode.Pixel;
			}
		});

		// change the paint mode to AREA mode
		tglBucket.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(false);
				tglBucket.setSelected(true);
				tglrec.setSelected(false);
				tgloval.setSelected(false);
				paintMode = PaintMode.Area;
			}
		});
		tglRD.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (int i = 0; i < data.length; i++)
					for (int j = 0; j < data[0].length; j++)
						data[i][j] = 0;
				change[1] = 3;
				paintPanel.repaint();
				tglRD.setSelected(false);
			}
		});
		tglrec.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(false);
				tglBucket.setSelected(false);
				tglrec.setSelected(true);
				tgloval.setSelected(false);
				paintMode = PaintMode.rec;
			}
		});
		tgloval.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(false);
				tglBucket.setSelected(false);
				tglrec.setSelected(false);
				tgloval.setSelected(true);

				paintMode = PaintMode.oval;
			}
		});

		tglSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				File dstFile = new File("kidPaint.txt");
				if (!dstFile.exists() || dstFile.isDirectory()) {
					try {
						dstFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					File fout = new File("KidPaint.txt");
					PrintWriter outStream= new PrintWriter(fout);
					for (int i = 0; i < 50; i++){
						for (int j = 0; j < 50; j++)
							outStream.printf("%d\n",data[i][j]);
					}
					outStream.close();
					System.out.println("Done");
				} catch (IOException e) {
					System.err.println("Fail to copy data from the source file to the destination file.");
				}
				str = "";
				tglSave.setSelected(false);
			}
		});

		JPanel msgPanel = new JPanel();

		getContentPane().add(msgPanel, BorderLayout.EAST);

		msgPanel.setLayout(new BorderLayout(0, 0));

		msgField = new JTextField(); // text field for inputting message

		msgPanel.add(msgField, BorderLayout.SOUTH);

		// handle key-input event of the message field
		msgField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10) { // if the user press ENTER
					msg = msgField.getText();
					onTextInputted(msgField.getText());
				//	System.out.println(msg);
					msgField.setText("");
					sent = false;
				}

			}

		});

		chatArea = new JTextArea(); // the read only text area for showing messages
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);

		JScrollPane scrollPaneRight = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneRight.setPreferredSize(new Dimension(300, this.getHeight()));
		msgPanel.add(scrollPaneRight, BorderLayout.CENTER);

		this.setSize(new Dimension(800, 600));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * it will be invoked if the user selected the specific color through the color
	 * picker
	 * 
	 * @param colorValue - the selected color
	 */
	public void selectColor(int colorValue) {
		SwingUtilities.invokeLater(() -> {
			selectedColor = colorValue;
			pnlColorPicker.setBackground(new Color(colorValue));
		});
	}

	/**
	 * it will be invoked if the user inputted text in the message field
	 * 
	 * @param text - user inputted text
	 */
	private void onTextInputted(String text) {
		chatArea.setText(chatArea.getText() + text + "\n");
	}

	/**
	 * change the color of a specific pixel
	 * 
	 * @param col, row - the position of the selected pixel
	 */
	public void paintPixel(int col, int row) {
		if (col >= data.length || row >= data[0].length)
			return;

		data[col][row] = selectedColor;
		paintPanel.repaint(col * blockSize, row * blockSize, blockSize, blockSize);

	}

	/**
	 * change the color of a specific area
	 * 
	 * @param col, row - the position of the selected pixel
	 * @return a list of modified pixels
	 */
	public List paintArea(int col, int row) {
		LinkedList<Point> filledPixels = new LinkedList<Point>();

		if (col >= data.length || row >= data[0].length)
			return filledPixels;

		int oriColor = data[col][row];
		LinkedList<Point> buffer = new LinkedList<Point>();

		if (oriColor != selectedColor) {
			buffer.add(new Point(col, row));

			while (!buffer.isEmpty()) {
				Point p = buffer.removeFirst();
				int x = p.x;
				int y = p.y;

				if (data[x][y] != oriColor)
					continue;

				data[x][y] = selectedColor;
				filledPixels.add(p);

				if (x > 0 && data[x - 1][y] == oriColor)
					buffer.add(new Point(x - 1, y));
				if (x < data.length - 1 && data[x + 1][y] == oriColor)
					buffer.add(new Point(x + 1, y));
				if (y > 0 && data[x][y - 1] == oriColor)
					buffer.add(new Point(x, y - 1));
				if (y < data[0].length - 1 && data[x][y + 1] == oriColor)
					buffer.add(new Point(x, y + 1));
			}
			paintPanel.repaint();
		}
		return filledPixels;
	}

	/**
	 * set pixel data and block size
	 * 
	 * @param data
	 * @param blockSize
	 */
	public void setData(int[][] data, int blockSize) {
		this.data = data;
		this.blockSize = blockSize;
		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));
		paintPanel.repaint();
	}

	public String getmsg() {
		if(sent == false){
			sent = true;
			return msg;
		}
		return "";
		
	}

	public int[][] getdata() {
		return data;
	}
	public boolean send(){
		return sent;
	}
	public int[] getchange() {
		int[] tmp = new int[10];
		for (int i = 1; i <= 8; i++)
			tmp[i] = change[i];

		return tmp;
	}

	public void changeit(int[] c) {
		int tmp = selectedColor;
		selectedColor = c[4];
		if (c[1] == 1)
			paintPixel(c[2] / blockSize, c[3] / blockSize);
		if (c[1] == 2)
			paintArea(c[2] / blockSize, c[3] / blockSize);
		if (c[1] == 3) {
			for (int i = 0; i < data.length; i++)
				for (int j = 0; j < data[0].length; j++)
					data[i][j] = 0;
		}
		if (c[1] == 4) {
			shapex = c[5];
			shapey = c[6];
			shapex2 = c[7];
			shapey2 = c[8];
			Rectangle();
			System.out.println("rec");
		}
		if (c[1] == 5) {
			shapex = c[5];
			shapey = c[6];
			shapex2 = c[7];
			shapey2 = c[8];
			Oval();
		}

		paintPanel.repaint();
		selectedColor = tmp;
	}

	public void pushdata(int[][] mydata) {
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++)
				data[i][j] = mydata[i][j];
		}
		paintPanel.repaint();
	}

	public void pushmsg(String tmp) {
		if(tmp != null)
			if(!tmp.isEmpty())
				chatArea.setText(chatArea.getText() + tmp + "\n");
	}

	public void cleandata() {
		for (int i = 1; i <= 8; i++)
			change[i] = -1;
	}

}