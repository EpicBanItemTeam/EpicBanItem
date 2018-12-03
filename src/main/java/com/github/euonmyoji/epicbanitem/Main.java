package com.github.euonmyoji.epicbanitem;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author yinyangshi GiNYAi ustc_zzzz
 */
public class Main {

    public static void main(String[] args) {
        new InfoFrame();
    }
}

/**
 * 糟糕的窗口类
 */
class InfoFrame extends JFrame {
    private final JButton openWiki = new JButton();
    private final JButton openOre = new JButton();

    InfoFrame() {
        this.setTitle("EpicBanItem v" + EpicBanItem.VERSION);
        this.setSize(400, 300);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(null);
        Container con = this.getContentPane();
        openWiki.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/euOnmyoji/EpicBanItem---Sponge/wiki"));
            } catch (URISyntaxException | IOException e1) {
                e1.printStackTrace();
            }
        });
        openWiki.setText("Open Wiki");

        openOre.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://ore.spongepowered.org/EpicBanItem/EpicBanItem"));
            } catch (URISyntaxException | IOException e1) {
                e1.printStackTrace();
            }
        });
        openOre.setText("Open Ore");

        con.add(openWiki);
        con.add(openOre);
        this.setVisible(true);
        repaint();

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Dimension size = this.getSize();
        double width = size.getWidth();
        double height = size.getHeight();
        g.setColor(Color.RED);
        g.setFont(new Font(null, Font.PLAIN, Math.min((int) (width * height / 10000), 48)));
        g.drawString("This is a Minecraft server plugin jar file!", (int) width / 4, (int) height / 3);
        openWiki.setBounds((int) width / 4, (int) height / 2, (int) width / 4, (int) height / 3);
        openOre.setBounds((int) width / 2, (int) height / 2, (int) width / 4, (int) height / 3);

    }
}
