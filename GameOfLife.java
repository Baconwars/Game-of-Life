package gameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;


public class GameOfLife extends JPanel implements MouseListener, MouseMotionListener, ActionListener, KeyListener {


    // WORLD SIZE
    int ROWS = 1000;
    int COLS = 1000;


    // ZOOM SIZE
    int CELL_SIZE = 5;



    HashSet<Point> grid;


    HashSet<Point>[] saves;




    // =========================
    // COPY SYSTEM
    // =========================

    HashSet<Point> clipboard =
        new HashSet<>();


    boolean copyMode = false;

    boolean pasteMode = false;


    Point copyStart = null;

    Point copyEnd = null;



    HashSet<Point> selectedArea =
        new HashSet<>();


    boolean showSelection = false;



    // =========================
    // ROTATION SYSTEM
    // =========================

    // 0 = normal
    // 1 = 90 degrees
    // 2 = 180 degrees
    // 3 = 270 degrees

    int rotation = 0;



    // Mouse position

    int mouseX = 0;

    int mouseY = 0;






    Timer timer;



    JButton startButton;
    JButton clearButton;
    JButton saveButton;
    JButton loadButton;
    JButton speedButton;

    JButton copyButton;
    JButton pasteButton;



    JComboBox<String> saveMenu;



    boolean running = false;







    // Speed

    int[] speeds = {

        3,
        16,
        66,
        200

    };



    String[] speedNames = {

        "300/s",
        "60/s",
        "15/s",
        "5/s"

    };



    int speedIndex = 0;







    public GameOfLife(){


        grid =
            new HashSet<>();



        saves =
            new HashSet[10];



        for(int i=0;i<10;i++){

            saves[i] =
                new HashSet<>();

        }





        updateSize();



        setBackground(
            Color.WHITE
        );



        addMouseListener(this);

        addMouseMotionListener(this);


        // keyboard for R rotation
        addKeyListener(this);

        setFocusable(true);




        timer =
            new Timer(
                speeds[speedIndex],
                this
            );


    }








    public void updateSize(){



        setPreferredSize(

            new Dimension(

                COLS * CELL_SIZE,

                ROWS * CELL_SIZE

            )

        );



        revalidate();

        repaint();


    }









    public void saveGame(int slot){


        saves[slot] =
            new HashSet<>(grid);


    }









    public void loadGame(int slot){


        grid =
            new HashSet<>(saves[slot]);



        repaint();


    }









    // =========================
    // COPY SELECTED AREA
    // =========================


    public void copySelected(){



        clipboard.clear();



        if(copyStart == null ||
           copyEnd == null)

            return;





        int minX =
            Math.min(
                copyStart.x,
                copyEnd.x
            );



        int maxX =
            Math.max(
                copyStart.x,
                copyEnd.x
            );





        int minY =
            Math.min(
                copyStart.y,
                copyEnd.y
            );



        int maxY =
            Math.max(
                copyStart.y,
                copyEnd.y
            );







        for(Point p:grid){



            if(p.x >= minX &&
               p.x <= maxX &&
               p.y >= minY &&
               p.y <= maxY){



                clipboard.add(

                    new Point(

                        p.x-minX,

                        p.y-minY

                    )

                );


            }


        }


    }







    // =========================
    // ROTATE COPY 90 DEGREE
    // =========================


    public void rotateClipboard(){


        HashSet<Point> rotated =
            new HashSet<>();


        for(Point p:clipboard){


            rotated.add(

                new Point(

                    -p.y,

                    p.x

                )

            );


        }



        // move back so preview starts at 0,0

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;



        for(Point p:rotated){


            if(p.x < minX)
                minX = p.x;


            if(p.y < minY)
                minY = p.y;

        }




        clipboard.clear();



        for(Point p:rotated){


            clipboard.add(

                new Point(

                    p.x-minX,

                    p.y-minY

                )

            );


        }



        rotation++;


        if(rotation >= 4)

            rotation = 0;


    }







    // =========================
    // PASTE PREVIEW
    // =========================


    public void drawPastePreview(Graphics g){



        if(!pasteMode)

            return;




        g.setColor(

            new Color(

                0,

                150,

                255,

                120

            )

        );





        for(Point p:clipboard){



            g.fillRect(

                (mouseX+p.x)
                *CELL_SIZE,


                (mouseY+p.y)
                *CELL_SIZE,


                CELL_SIZE,


                CELL_SIZE

            );


        }


    }
    @Override
    protected void paintComponent(Graphics g){



        super.paintComponent(g);




        g.setColor(
            Color.WHITE
        );


        g.fillRect(

            0,

            0,

            getWidth(),

            getHeight()

        );






        // DRAW ALIVE CELLS

        g.setColor(
            Color.BLACK
        );



        for(Point p:grid){


            g.fillRect(

                p.x * CELL_SIZE,

                p.y * CELL_SIZE,

                CELL_SIZE,

                CELL_SIZE

            );


        }








        // BLUE COPY AREA

        if(showSelection){



            g.setColor(

                new Color(

                    0,

                    100,

                    255,

                    100

                )

            );



            for(Point p:selectedArea){



                g.fillRect(

                    p.x * CELL_SIZE,

                    p.y * CELL_SIZE,

                    CELL_SIZE,

                    CELL_SIZE

                );


            }


        }







        // BLUE ROTATED PREVIEW

        drawPastePreview(g);







        // COORDINATES

        g.setColor(
            Color.RED
        );


        g.drawString(

            "Mouse: X="
            +mouseX+
            " Y="
            +mouseY,


            10,

            15

        );


    }









    private void nextGeneration(){



        HashMap<Point,Integer> neighbors =

            new HashMap<>();






        for(Point cell:grid){



            for(int x=-1;x<=1;x++){



                for(int y=-1;y<=1;y++){



                    if(x==0 && y==0)

                        continue;





                    Point p =

                        new Point(

                            cell.x+x,

                            cell.y+y

                        );





                    neighbors.put(

                        p,

                        neighbors.getOrDefault(

                            p,

                            0

                        ) + 1

                    );



                }


            }


        }







        HashSet<Point> next =

            new HashSet<>();






        for(Map.Entry<Point,Integer> entry:

            neighbors.entrySet()){



            Point p =
                entry.getKey();



            int amount =
                entry.getValue();





            if(amount == 3 ||

              (amount == 2 &&
               grid.contains(p))){



                next.add(p);


            }


        }





        grid = next;


    }









    @Override
    public void mousePressed(MouseEvent e){



        if(running)

            return;





        int col =

            e.getX() / CELL_SIZE;



        int row =

            e.getY() / CELL_SIZE;





        Point p =

            new Point(

                col,

                row

            );







        // =====================
        // COPY MODE
        // =====================


        if(copyMode){



            if(copyStart == null){


                copyStart = p;


            }


            else{


                copyEnd = p;



                selectedArea.clear();




                int minX =

                    Math.min(

                        copyStart.x,

                        copyEnd.x

                    );



                int maxX =

                    Math.max(

                        copyStart.x,

                        copyEnd.x

                    );



                int minY =

                    Math.min(

                        copyStart.y,

                        copyEnd.y

                    );



                int maxY =

                    Math.max(

                        copyStart.y,

                        copyEnd.y

                    );






                for(int y=minY;y<=maxY;y++){


                    for(int x=minX;x<=maxX;x++){


                        selectedArea.add(

                            new Point(

                                x,

                                y

                            )

                        );


                    }


                }







                copySelected();



                copyMode=false;



                showSelection=true;



                repaint();







                Timer remove =

                    new Timer(

                        1000,

                        event -> {


                            showSelection=false;


                            selectedArea.clear();


                            repaint();


                        }

                    );



                remove.setRepeats(false);


                remove.start();



            }



            return;


        }









        // =====================
        // PASTE MODE
        // =====================


        if(pasteMode){



            for(Point cell:clipboard){



                grid.add(

                    new Point(

                        mouseX + cell.x,

                        mouseY + cell.y

                    )

                );


            }




            pasteMode=false;



            rotation = 0;



            repaint();



            return;


        }









        // NORMAL DRAWING



        if(grid.contains(p))


            grid.remove(p);


        else


            grid.add(p);




        repaint();


    }









    @Override
    public void mouseMoved(MouseEvent e){



        mouseX =

            e.getX()/CELL_SIZE;



        mouseY =

            e.getY()/CELL_SIZE;




        repaint();


    }
    @Override
    public void keyPressed(KeyEvent e){


        // ROTATE WHILE PASTING

        if(e.getKeyCode() == KeyEvent.VK_R
            && pasteMode){



            rotateClipboard();


            repaint();


        }


    }




    @Override
    public void keyReleased(KeyEvent e){}



    @Override
    public void keyTyped(KeyEvent e){}









    @Override
    public void actionPerformed(ActionEvent e){



        if(e.getSource()==timer){



            nextGeneration();


            repaint();



        }







        else if(e.getSource()==startButton){



            running = !running;




            if(running){



                timer.start();


                startButton.setText(
                    "Pause"
                );



            }


            else{


                timer.stop();


                startButton.setText(
                    "Start"
                );


            }


        }








        else if(e.getSource()==clearButton){



            timer.stop();



            running=false;



            startButton.setText(
                "Start"
            );



            grid.clear();



            repaint();



        }


    }







    @Override
    public void mouseDragged(MouseEvent e){}



    @Override
    public void mouseClicked(MouseEvent e){}



    @Override
    public void mouseReleased(MouseEvent e){}



    @Override
    public void mouseEntered(MouseEvent e){}



    @Override
    public void mouseExited(MouseEvent e){}










    public static void main(String[] args){



        SwingUtilities.invokeLater(() -> {



            JFrame frame =
                new JFrame(
                    "Conway's Game of Life"
                );





            GameOfLife game =
                new GameOfLife();





            JScrollPane scroll =
                new JScrollPane(game);




            scroll.setPreferredSize(

                new Dimension(

                    800,

                    600

                )

            );







            JPanel buttons =
                new JPanel();








            game.startButton =
                new JButton("Start");


            game.clearButton =
                new JButton("Clear");


            game.saveButton =
                new JButton("Save");


            game.loadButton =
                new JButton("Load");


            game.speedButton =
                new JButton(
                    "Speed: 300/s"
                );



            game.copyButton =
                new JButton(
                    "Copy"
                );


            game.pasteButton =
                new JButton(
                    "Paste"
                );









            game.saveMenu =
                new JComboBox<>();



            for(int i=1;i<=10;i++){


                game.saveMenu.addItem(

                    "Save " + i

                );


            }









            game.startButton.addActionListener(game);


            game.clearButton.addActionListener(game);









            // COPY BUTTON


            game.copyButton.addActionListener(e -> {



                game.copyMode=true;



                game.copyStart=null;



                game.copyEnd=null;



                game.rotation=0;



            });









            // PASTE BUTTON


            game.pasteButton.addActionListener(e -> {



                if(!game.clipboard.isEmpty()){



                    game.pasteMode=true;



                    game.rotation=0;



                    game.requestFocus();


                }



            });









            // SPEED BUTTON


            game.speedButton.addActionListener(e -> {



                game.speedIndex++;



                if(game.speedIndex >= game.speeds.length)

                    game.speedIndex=0;





                game.timer.setDelay(

                    game.speeds[game.speedIndex]

                );





                game.speedButton.setText(

                    "Speed: "
                    +
                    game.speedNames[game.speedIndex]

                );



            });









            game.saveButton.addActionListener(e -> {



                game.saveGame(

                    game.saveMenu.getSelectedIndex()

                );


            });








            game.loadButton.addActionListener(e -> {



                game.loadGame(

                    game.saveMenu.getSelectedIndex()

                );


            });









            // SCALE

            JSlider scaleSlider =

                new JSlider(

                    5,

                    50,

                    5

                );





            scaleSlider.setMajorTickSpacing(5);


            scaleSlider.setPaintTicks(true);


            scaleSlider.setPaintLabels(true);








            scaleSlider.addChangeListener(e -> {



                int oldSize =
                    game.CELL_SIZE;





                Point old =
                    scroll.getViewport()
                    .getViewPosition();





                int gridX =
                    old.x / oldSize;



                int gridY =
                    old.y / oldSize;






                game.CELL_SIZE =
                    scaleSlider.getValue();





                game.updateSize();







                SwingUtilities.invokeLater(() -> {



                    scroll.getViewport()
                    .setViewPosition(


                        new Point(

                            gridX *
                            game.CELL_SIZE,


                            gridY *
                            game.CELL_SIZE

                        )


                    );


                });



            });









            buttons.add(game.startButton);


            buttons.add(game.clearButton);


            buttons.add(game.copyButton);


            buttons.add(game.pasteButton);


            buttons.add(game.saveButton);


            buttons.add(game.loadButton);


            buttons.add(game.speedButton);


            buttons.add(game.saveMenu);



            buttons.add(
                new JLabel(
                    "Cell Size:"
                )
            );



            buttons.add(scaleSlider);









            frame.setLayout(

                new BorderLayout()

            );





            frame.add(

                scroll,

                BorderLayout.CENTER

            );





            frame.add(

                buttons,

                BorderLayout.SOUTH

            );






            frame.pack();



            frame.setResizable(true);




            frame.setDefaultCloseOperation(

                JFrame.EXIT_ON_CLOSE

            );




            frame.setLocationRelativeTo(null);



            frame.setVisible(true);



        });


    }

}