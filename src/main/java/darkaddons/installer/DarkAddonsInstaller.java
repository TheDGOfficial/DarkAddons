package darkaddons.installer;

import gg.darkaddons.AccessibleObjectResource;
import gg.darkaddons.PublicUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DarkAddonsInstaller extends JFrame implements ActionListener, MouseListener {
    @NotNull
    private static final Logger LOGGER = Logger.getLogger(DarkAddonsInstaller.class.getName());

    private static final long serialVersionUID = 0L;

    @NotNull
    private static final Pattern IN_MODS_SUBFOLDER = Pattern.compile("1\\.8\\.9[/\\\\]?$");

    @NotNull
    private JLabel logo;
    @NotNull
    private JLabel versionInfo;
    @NotNull
    private JLabel labelFolder;

    @NotNull
    private JPanel panelCenter;
    @NotNull
    private JPanel panelBottom;
    @NotNull
    private JPanel totalContentPane;

    @NotNull
    private JTextArea descriptionText;
    @NotNull
    private JTextArea forgeDescriptionText;

    @NotNull
    private JTextField textFieldFolderLocation;
    @NotNull
    private JButton buttonChooseFolder;

    @NotNull
    private JButton buttonInstall;
    @NotNull
    private JButton buttonOpenFolder;
    @NotNull
    private JButton buttonClose;

    private static final int TOTAL_HEIGHT = 435;
    private static final int TOTAL_WIDTH = 404;

    private int xCoord;
    private int yCoord;

    private int w = DarkAddonsInstaller.TOTAL_WIDTH;
    private int h;
    private int margin;

    private DarkAddonsInstaller() {
        super();
        try {
            this.setName("DarkAddonsInstaller");
            this.setTitle("DarkAddons Installer");
            this.setResizable(false);
            this.setSize(DarkAddonsInstaller.TOTAL_WIDTH, DarkAddonsInstaller.TOTAL_HEIGHT);
            this.setContentPane(this.getPanelContentPane());

            this.getButtonFolder().addActionListener(this);
            this.getButtonInstall().addActionListener(this);
            this.getButtonOpenFolder().addActionListener(this);
            this.getButtonClose().addActionListener(this);
            this.getForgeTextArea().addMouseListener(this);

            this.pack();
            this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            this.getFieldFolder().setText(DarkAddonsInstaller.getModsFolder().getPath());
            this.getButtonInstall().setEnabled(true);
            this.getButtonInstall().requestFocus();
        } catch (final Exception ex) {
            DarkAddonsInstaller.showErrorPopup(ex);
        }
    }

    public static final void main(final String... args) {
        try {
            final var xToolkit = DarkAddonsInstaller.getxToolkit();

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            final var frame = new DarkAddonsInstaller();
            DarkAddonsInstaller.centerFrame(frame);

            try {
                final var url = ClassLoader.getSystemResource("assets/darkaddons/darkaddons.icon.png");
                frame.setIconImage(xToolkit.createImage(url));
            } catch (final Throwable tw) {
                DarkAddonsInstaller.LOGGER.log(Level.SEVERE, tw, tw::getMessage);
                // Continue using the default Java coffee icon, I guess...
            }

            frame.setVisible(true);
        } catch (final Exception ex) {
            DarkAddonsInstaller.showErrorPopup(ex);
        }
    }

    @Nullable
    private static final Toolkit getxToolkit() {
        final var xToolkit = Toolkit.getDefaultToolkit();

        // Set WM_CLASS
        try {
            final var awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
            try (final var accessibleObjectResource = new AccessibleObjectResource<>(awtAppClassNameField)) {
                accessibleObjectResource.grantAccess(); // TODO fails on Java >= 16
                awtAppClassNameField.set(xToolkit, "DarkAddons Installer");
            }
        } catch (final Throwable tw) {
            // Continue sharing the same WM_CLASS for every Java app, I guess...
        }
        return xToolkit;
    }

    @NotNull
    private final JPanel getPanelContentPane() {
        if (null == this.totalContentPane) {
            try {
                this.totalContentPane = new JPanel();
                this.totalContentPane.setName("PanelContentPane");
                this.totalContentPane.setLayout(new BorderLayout(5, 5));
                this.totalContentPane.setPreferredSize(new Dimension(DarkAddonsInstaller.TOTAL_WIDTH, DarkAddonsInstaller.TOTAL_HEIGHT));
                this.totalContentPane.add(this.getPanelCenter(), "Center");
                this.totalContentPane.add(this.getPanelBottom(), "South");
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }
        }
        return this.totalContentPane;
    }

    @NotNull
    private final JPanel getPanelCenter() {
        if (null == this.panelCenter) {
            try {
                (this.panelCenter = new JPanel()).setName("PanelCenter");
                this.panelCenter.setLayout(null);
                this.panelCenter.add(this.getPictureLabel(), this.getPictureLabel().getName());
                this.panelCenter.add(this.getVersionInfo(), this.getVersionInfo().getName());
                this.panelCenter.add(this.getTextArea(), this.getTextArea().getName());
                this.panelCenter.add(this.getForgeTextArea(), this.getForgeTextArea().getName());
                this.panelCenter.add(this.getLabelFolder(), this.getLabelFolder().getName());
                this.panelCenter.add(this.getFieldFolder(), this.getFieldFolder().getName());
                this.panelCenter.add(this.getButtonFolder(), this.getButtonFolder().getName());
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }
        }
        return this.panelCenter;
    }

    @NotNull
    private final JLabel getPictureLabel() {
        if (null == this.logo) {
            try {
                this.h = this.w >> 1;
                this.margin = 5;

                final var myPicture = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader()
                    .getResourceAsStream("assets/darkaddons/darkaddons.logo.png"), "Logo not found."));
                final var scaled = myPicture.getScaledInstance(this.w - (this.margin << 1), this.h - this.margin, Image.SCALE_SMOOTH);
                this.logo = new JLabel(new ImageIcon(scaled));
                this.logo.setName("Logo");
                this.logo.setBounds(this.xCoord + this.margin, this.yCoord + this.margin, this.w - (this.margin << 1), this.h - this.margin);
                this.logo.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
                this.logo.setHorizontalAlignment(SwingConstants.CENTER);
                this.logo.setPreferredSize(new Dimension(this.w, this.h));

                this.yCoord += this.h;
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }
        }
        return this.logo;
    }

    @NotNull
    private final JLabel getVersionInfo() {
        if (null == this.versionInfo) {
            try {
                this.h = 25;

                this.versionInfo = new JLabel();
                this.versionInfo.setName("LabelMcVersion");
                this.versionInfo.setBounds(this.xCoord, this.yCoord, this.w, this.h);
                this.versionInfo.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
                this.versionInfo.setHorizontalAlignment(SwingConstants.CENTER);
                this.versionInfo.setPreferredSize(new Dimension(this.w, this.h));
                this.versionInfo.setText('v' + this.getVersionFromMcmodInfo() + " for Minecraft 1.8.9");

                this.yCoord += this.h;
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }
        }
        return this.versionInfo;
    }

    @NotNull
    private final JTextArea getTextArea() {
        if (null == this.descriptionText) {
            try {
                this.h = 60;
                this.margin = 10;

                this.descriptionText = new JTextArea();
                this.descriptionText.setName("TextArea");
                this.setTextAreaProperties(this.descriptionText);
                this.descriptionText.setText("This installer will copy DarkAddons into your forge mods folder for you, and replace any old versions that already exist. " +
                    "Close this if you prefer to do this yourself!");
                this.descriptionText.setWrapStyleWord(true);

                this.yCoord += this.h;
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }
        }
        return this.descriptionText;
    }

    private final void setTextAreaProperties(final JTextArea textArea) {
        textArea.setBounds(this.xCoord + this.margin, this.yCoord + this.margin, this.w - (this.margin << 1), this.h - this.margin);
        textArea.setEditable(false);
        textArea.setHighlighter(null);
        textArea.setEnabled(true);
        textArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setPreferredSize(new Dimension(this.w - (this.margin << 1), this.h - this.margin));
    }

    @NotNull
    private final JTextArea getForgeTextArea() {
        if (null == this.forgeDescriptionText) {
            try {
                this.h = 55;
                this.margin = 10;

                this.forgeDescriptionText = new JTextArea();
                this.forgeDescriptionText.setName("TextAreaForge");
                this.setTextAreaProperties(this.forgeDescriptionText);
                this.forgeDescriptionText.setText("However, you still need to install Forge client in order to be able to run this mod. Click here to visit the download page for Forge 1.8.9!");
                this.forgeDescriptionText.setForeground(Color.BLUE.darker());
                this.forgeDescriptionText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                this.forgeDescriptionText.setWrapStyleWord(true);

                this.yCoord += this.h;
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }
        }
        return this.forgeDescriptionText;
    }

    @NotNull
    private final JLabel getLabelFolder() {
        if (null == this.labelFolder) {
            this.h = 16;
            this.w = 65;

            this.xCoord += 10; // Padding

            try {
                this.labelFolder = new JLabel();
                this.labelFolder.setName("LabelFolder");
                this.labelFolder.setBounds(this.xCoord, this.yCoord + 2, this.w, this.h);
                this.labelFolder.setPreferredSize(new Dimension(this.w, this.h));
                this.labelFolder.setText("Mods Folder");
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }

            this.xCoord += this.w;
        }
        return this.labelFolder;
    }

    @NotNull
    private final JTextField getFieldFolder() {
        if (null == this.textFieldFolderLocation) {
            this.h = 20;
            this.w = 287;

            try {
                this.textFieldFolderLocation = new JTextField();
                this.textFieldFolderLocation.setName("FieldFolder");
                this.textFieldFolderLocation.setBounds(this.xCoord, this.yCoord, this.w, this.h);
                this.textFieldFolderLocation.setEditable(false);
                this.textFieldFolderLocation.setPreferredSize(new Dimension(this.w, this.h));
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }

            this.xCoord += this.w;
        }
        return this.textFieldFolderLocation;
    }

    @NotNull
    private final JButton getButtonFolder() {
        if (null == this.buttonChooseFolder) {
            this.h = 20;
            this.w = 25;

            this.xCoord += 10; // Padding

            try {
                final var myPicture = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader()
                    .getResourceAsStream("assets/darkaddons/folder_icon_for_installer.png"), "Folder icon not found."));
                final var scaled = myPicture.getScaledInstance(this.w - 8, this.h - 6, Image.SCALE_SMOOTH);
                this.buttonChooseFolder = new JButton(new ImageIcon(scaled));
                this.buttonChooseFolder.setName("ButtonFolder");
                this.buttonChooseFolder.setBounds(this.xCoord, this.yCoord, this.w, this.h);
                this.buttonChooseFolder.setPreferredSize(new Dimension(this.w, this.h));
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }
        }
        return this.buttonChooseFolder;
    }

    @NotNull
    private final JPanel getPanelBottom() {
        if (null == this.panelBottom) {
            try {
                this.panelBottom = new JPanel();
                this.panelBottom.setName("PanelBottom");
                this.panelBottom.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
                this.panelBottom.setPreferredSize(new Dimension(390, 55));
                this.panelBottom.add(this.getButtonInstall(), this.getButtonInstall().getName());
                this.panelBottom.add(this.getButtonOpenFolder(), this.getButtonOpenFolder().getName());
                this.panelBottom.add(this.getButtonClose(), this.getButtonClose().getName());
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }
        }
        return this.panelBottom;
    }

    @NotNull
    private final JButton getButtonInstall() {
        if (null == this.buttonInstall) {
            this.w = 100;
            this.h = 26;

            try {
                this.buttonInstall = new JButton();
                this.buttonInstall.setName("ButtonInstall");
                this.buttonInstall.setPreferredSize(new Dimension(this.w, this.h));
                this.buttonInstall.setText("Install");
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }
        }
        return this.buttonInstall;
    }

    @NotNull
    private final JButton getButtonOpenFolder() {
        if (null == this.buttonOpenFolder) {
            this.w = 130;
            this.h = 26;

            try {
                this.buttonOpenFolder = new JButton();
                this.buttonOpenFolder.setName("ButtonOpenFolder");
                this.buttonOpenFolder.setPreferredSize(new Dimension(this.w, this.h));
                this.buttonOpenFolder.setText("Open Mods Folder");
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }
        }
        return this.buttonOpenFolder;
    }

    @NotNull
    private final JButton getButtonClose() {
        if (null == this.buttonClose) {
            this.w = 100;
            this.h = 26;

            try {
                (this.buttonClose = new JButton()).setName("ButtonClose");
                this.buttonClose.setPreferredSize(new Dimension(this.w, this.h));
                this.buttonClose.setText("Cancel");
            } catch (final Throwable ivjExc) {
                DarkAddonsInstaller.showErrorPopup(ivjExc);
            }
        }
        return this.buttonClose;
    }

    private final void onFolderSelect() {
        final var currentDirectory = new File(this.getFieldFolder().getText());

        final var jFileChooser = new JFileChooser(currentDirectory);
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setAcceptAllFileFilterUsed(false);
        if (0 == jFileChooser.showOpenDialog(this)) {
            final var newDirectory = jFileChooser.getSelectedFile();
            this.getFieldFolder().setText(newDirectory.getPath());
        }
    }

    @SuppressWarnings("ObjectEquality")
    @Override
    public final void actionPerformed(final ActionEvent e) {
        if (e.getSource() == this.getButtonClose()) {
            this.dispose();
            //noinspection CallToSystemExit
            System.exit(0);
        }
        if (e.getSource() == this.getButtonFolder()) {
            this.onFolderSelect();
        }
        if (e.getSource() == this.getButtonInstall()) {
            this.onInstall();
        }
        if (e.getSource() == this.getButtonOpenFolder()) {
            DarkAddonsInstaller.onOpenFolder();
        }
    }

    @Override
    public final void mouseClicked(final MouseEvent e) {
        //noinspection ObjectEquality
        if (e.getSource() == this.getForgeTextArea()) {
            try {
                Desktop.getDesktop().browse(new URI("https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.8.9.html"));
            } catch (final IOException | URISyntaxException ex) {
                DarkAddonsInstaller.showErrorPopup(ex);
            }
        }
    }

    private final void onInstall() {
        try {
            final var modsFolder = new File(this.getFieldFolder().getText());
            if (!modsFolder.exists()) {
                DarkAddonsInstaller.showErrorMessage("Folder not found: " + modsFolder.getPath());
                return;
            }
            if (!modsFolder.isDirectory()) {
                DarkAddonsInstaller.showErrorMessage("Not a folder: " + modsFolder.getPath());
                return;
            }
            this.tryInstall(modsFolder);
        } catch (final Exception e) {
            DarkAddonsInstaller.showErrorPopup(e);
        }
    }

    private final void tryInstall(@NotNull final File modsFolder) {
        final var thisFile = DarkAddonsInstaller.getThisFile();

        if (null != thisFile) {
            final var inSubFolder = DarkAddonsInstaller.IN_MODS_SUBFOLDER.matcher(modsFolder.getPath()).find();

            final var newFile = new File(modsFolder, "DarkAddons-" + this.getVersionFromMcmodInfo() + "-opt.jar");
            if (thisFile.equals(newFile)) {
                DarkAddonsInstaller.showErrorMessage("You are opening this file from where the file should be installed... there's nothing to be done!");
                return;
            }

            var deletingFailure = false;
            if (modsFolder.isDirectory()) { // Delete in this current folder.
                final var failed = DarkAddonsInstaller.findDarkAddonsAndDelete(modsFolder.listFiles());
                if (failed) {
                    deletingFailure = true;
                }
            }
            if (inSubFolder) { // We are in the 1.8.9 folder, delete in the parent folder as well.
                final var parentFile = modsFolder.getParentFile();
                if (null != parentFile && parentFile.isDirectory()) {
                    final var failed = DarkAddonsInstaller.findDarkAddonsAndDelete(parentFile.listFiles());
                    if (failed) {
                        deletingFailure = true;
                    }
                }
            } else { // We are in the main mods folder, but the 1.8.9 subfolder exists... delete in there too.
                final var subFolder = new File(modsFolder, "1.8.9");
                if (subFolder.exists() && subFolder.isDirectory()) {
                    final var failed = DarkAddonsInstaller.findDarkAddonsAndDelete(subFolder.listFiles());
                    if (failed) {
                        deletingFailure = true;
                    }
                }
            }

            if (deletingFailure) {
                return;
            }

            if (thisFile.isDirectory()) {
                DarkAddonsInstaller.showErrorMessage("This file is a directory... Are we in a development environment?");
                return;
            }

            try {
                Files.copy(thisFile.toPath(), newFile.toPath());
            } catch (final Exception ex) {
                DarkAddonsInstaller.showErrorPopup(ex);
                return;
            }

            DarkAddonsInstaller.showMessage("DarkAddons has been successfully installed into your mods folder.");
            this.dispose();
            //noinspection CallToSystemExit
            System.exit(0);
        }
    }

    private static final boolean findDarkAddonsAndDelete(@NotNull final File... files) {
        if (null == files) {
            return false;
        }

        for (final var file : files) {
            if (!file.isDirectory() && file.getPath().endsWith(".jar")) {
                try (final var jarFile = new JarFile(file)) {
                    final var mcModInfo = jarFile.getEntry("mcmod.info");
                    if (null != mcModInfo) {
                        try (final var inputStream = jarFile.getInputStream(mcModInfo)) {
                            final var modID = DarkAddonsInstaller.getModIDFromInputStream(inputStream);
                            if ("darkaddons".equals(modID)) {
                                try {
                                    Files.delete(file.toPath());
                                } catch (final Exception ex) {
                                    DarkAddonsInstaller.LOGGER.log(Level.SEVERE, ex, ex::getMessage);
                                    DarkAddonsInstaller.showErrorMessage("Was not able to delete the other DarkAddons files found in your mods folder!" + System.lineSeparator() +
                                    "Please make sure that your minecraft is currently closed and try again, or feel" + System.lineSeparator() +
                                    "free to open your mods folder and delete those files manually.");
                                    return true;
                                }
                            }
                        }
                    }
                } catch (final Exception ex) {
                    // Just don't check the file; I guess move on to the next...
                }
            }
        }
        return false;
    }

    private static final void onOpenFolder() {
        try {
            Desktop.getDesktop().open(DarkAddonsInstaller.getModsFolder());
        } catch (final Exception e) {
            DarkAddonsInstaller.showErrorPopup(e);
        }
    }

    private static final File getModsFolder() {
        final var userHome = System.getProperty("user.home", ".");

        var modsFolder = DarkAddonsInstaller.getFile(userHome, "minecraft/mods/1.8.9");
        if (!modsFolder.exists()) {
            modsFolder = DarkAddonsInstaller.getFile(userHome, "minecraft/mods");
        }

        //noinspection IfCanBeAssertion
        if (!modsFolder.exists() && !modsFolder.mkdirs()) {
            throw new UncheckedIOException(new IOException("The mods directory could not be created: " + modsFolder));
        }
        return modsFolder;
    }

    @NotNull
    private static final File getFile(@NotNull final String userHome, @NotNull final String minecraftPath) {
        final var os = DarkAddonsInstaller.getOperatingSystem();

        if (DarkAddonsInstaller.OperatingSystem.LINUX == os || DarkAddonsInstaller.OperatingSystem.SOLARIS == os) {
            return new File(userHome, '.' + minecraftPath + '/');
        }

        if (DarkAddonsInstaller.OperatingSystem.WINDOWS == os) {
            final var applicationData = System.getenv("APPDATA");
            return new File(null == applicationData ? userHome : applicationData, '.' + minecraftPath + '/');
        }

        return new File(userHome, DarkAddonsInstaller.OperatingSystem.MACOS == os ? "Library/Application Support/" + minecraftPath : minecraftPath + '/');
    }

    @NotNull
    private static final DarkAddonsInstaller.OperatingSystem getOperatingSystem() {
        final var osName = System.getProperty("os.name").toLowerCase(Locale.US);
        if (osName.contains("win")) {
            return DarkAddonsInstaller.OperatingSystem.WINDOWS;

        }
        if (osName.contains("mac")) {
            return DarkAddonsInstaller.OperatingSystem.MACOS;

        }
        if (osName.contains("solaris") || osName.contains("sunos")) {

            return DarkAddonsInstaller.OperatingSystem.SOLARIS;
        }
        return osName.contains("linux") || osName.contains("unix") ? DarkAddonsInstaller.OperatingSystem.LINUX : DarkAddonsInstaller.OperatingSystem.UNKNOWN;
    }

    private static final void centerFrame(final DarkAddonsInstaller frame) {
        final var rectangle = frame.getBounds();
        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final var screenRectangle = new Rectangle(0, 0, screenSize.width, screenSize.height);

        var newX = screenRectangle.x + (screenRectangle.width - rectangle.width >> 1);
        var newY = screenRectangle.y + (screenRectangle.height - rectangle.height >> 1);

        if (0 > newX) {
            newX = 0;
        }
        if (0 > newY) {
            newY = 0;
        }

        frame.setBounds(newX, newY, rectangle.width, rectangle.height);
    }

    private static final void showMessage(@NotNull final String message) {
        JOptionPane.showMessageDialog(null, message, "DarkAddons", JOptionPane.INFORMATION_MESSAGE);
    }

    private static final void showErrorMessage(@NotNull final String message) {
        JOptionPane.showMessageDialog(null, message, "DarkAddons - Error", JOptionPane.ERROR_MESSAGE);
    }

    private enum OperatingSystem {
        LINUX,
        SOLARIS,
        WINDOWS,
        MACOS,
        UNKNOWN;

        private OperatingSystem() {
        }
    }

    @NotNull
    private static final Matcher tabMatcher = Pattern.compile("\t", Pattern.LITERAL).matcher("");

    @NotNull
    private static final String getStacktraceText(@NotNull final Throwable ex) {
        try (final var stringWriter = new StringWriter()) {
            try (final var printWriter = new PrintWriter(stringWriter)) {
                ex.printStackTrace(printWriter);
            }
            return DarkAddonsInstaller.tabMatcher.reset(stringWriter.toString()).replaceAll("  ");
        } catch (final IOException ioException) {
            throw new UncheckedIOException(ioException);
        }
    }

    private static final void showErrorPopup(@NotNull final Throwable ex) {
        DarkAddonsInstaller.LOGGER.log(Level.SEVERE, ex, ex::getMessage);

        final var textArea = new JTextArea(DarkAddonsInstaller.getStacktraceText(ex));
        textArea.setEditable(false);
        final var currentFont = textArea.getFont();
        final var newFont = new Font(Font.MONOSPACED, currentFont.getStyle(), currentFont.getSize());
        textArea.setFont(newFont);

        final var errorScrollPane = new JScrollPane(textArea);
        errorScrollPane.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(null, errorScrollPane, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @NotNull
    private final String getVersionFromMcmodInfo() {
        var version = "";
        try (final var inputStreamReader = new InputStreamReader(Objects.requireNonNull(this.getClass().
                getClassLoader().getResourceAsStream("mcmod.info"), "mcmod.info not found."), StandardCharsets.UTF_8)) {
            try (final var bufferedReader = new BufferedReader(inputStreamReader)) {
                while (null != (version = bufferedReader.readLine())) {
                    if (version.contains("\"version\": \"")) {
                        version = version.split(Pattern.quote("\"version\": \""))[1];
                        return version.substring(0, version.length() - 2);
                    }
                }
            }
        } catch (final Exception ex) {
            // It's okay; I guess just don't use the version lol.
        }
        return version;
    }

    @NotNull
    private static final String getModIDFromInputStream(@NotNull final InputStream inputStream) {
        var version = "";
        try (final var inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            try (final var bufferedReader = new BufferedReader(inputStreamReader)) {
                while (null != (version = bufferedReader.readLine())) {
                    if (version.contains("\"modid\": \"")) {
                        version = version.split(Pattern.quote("\"modid\": \""))[1];
                        return version.substring(0, version.length() - 2);
                    }
                }
            }
        } catch (final Exception ex) {
            // RIP, couldn't find the modid...
        }
        return version;
    }

    @Nullable
    private static final File getThisFile() {
        try {
            return new File(DarkAddonsInstaller.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (final URISyntaxException ex) {
            DarkAddonsInstaller.showErrorPopup(ex);
        }
        return null;
    }

    @Override
    public final void mousePressed(@SuppressWarnings("NullableProblems") @NotNull final MouseEvent e) {
        // do nothing
    }

    @Override
    public final void mouseReleased(@SuppressWarnings("NullableProblems") @NotNull final MouseEvent e) {
        // do nothing
    }

    @Override
    public final void mouseEntered(@SuppressWarnings("NullableProblems") @NotNull final MouseEvent e) {
        // do nothing
    }

    @Override
    public final void mouseExited(@SuppressWarnings("NullableProblems") @NotNull final MouseEvent e) {
        // do nothing
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public final String toString() {
        return "DarkAddonsInstaller{" +
            "logo=" + this.logo +
            ", versionInfo=" + this.versionInfo +
            ", labelFolder=" + this.labelFolder +
            ", panelCenter=" + this.panelCenter +
            ", panelBottom=" + this.panelBottom +
            ", totalContentPane=" + this.totalContentPane +
            ", descriptionText=" + this.descriptionText +
            ", forgeDescriptionText=" + this.forgeDescriptionText +
            ", textFieldFolderLocation=" + this.textFieldFolderLocation +
            ", buttonChooseFolder=" + this.buttonChooseFolder +
            ", buttonInstall=" + this.buttonInstall +
            ", buttonOpenFolder=" + this.buttonOpenFolder +
            ", buttonClose=" + this.buttonClose +
            ", x=" + this.xCoord +
            ", y=" + this.yCoord +
            ", w=" + this.w +
            ", h=" + this.h +
            ", margin=" + this.margin +
            '}';
    }
}

