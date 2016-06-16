//monitorgröße : 1366 * 768
//fenstergröße : 1382 * 784
package sbv;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import static sbv.Sbv.logger;

public class Oberflaeche extends javax.swing.JFrame {

    public final Font format = new Font(FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD));

    private static final String einsammelnCol[] = {"N", "Label", "Code", "Name", "Klasse", "Ausgegeben", "Gekauft", "Bezahlt"};
    DefaultTableModel einsammelTabelleModel = new DefaultTableModel(einsammelnCol, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    static private final int einsammelnColSize[] = {25, 409, 55, 160, 90, 80, 55, 55};
    static private boolean einsammelnColSizeSet = true;

    private static final String schulerCol[] = {"Nachname", "Vorname", "Geburtsdatum"};
    DefaultTableModel schuelerModel = new DefaultTableModel(schulerCol, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    static private final int schulerColSize[] = {80, 80, 50};
    static private boolean schulerColSizeSet = true;

    private static final String buecherCol[] = {"Label", "ISBN", "Preis", "Kaufbuch"};
    DefaultTableModel buecherModel = new DefaultTableModel(buecherCol, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    static private final int buecherColSize[] = {409, 80, 55, 55};
    static private boolean buecherColSizeSet = true;

    private static final String buecherKlasseCol[] = {"Label", "ISBN"};
    DefaultTableModel buecherKlasseModel = new DefaultTableModel(buecherKlasseCol, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private static final String schuelerBuecherCol[] = {"N", "Label", "Gekauft", "Ausgegeben", "Bezahlt", "Barcode"};
    DefaultTableModel schuelerBuecherModel = new DefaultTableModel(schuelerBuecherCol, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    static private final int schuelerBuecherColSize[] = {25, 409, 55, 80, 55, 55};
    static private boolean schuelerBuecherColSizeSet = true;

    DefaultListModel pdfExportAuswahlAllesListModel = new DefaultListModel();
    DefaultListModel pdfExportAuswahlSelectListModel = new DefaultListModel();

    private static final String pdfExportSchuelerOpList[] = {"Label", "Gekauft", "Ausgegeben", "Bezahlt", "Code", "ISBN", "Preis", "zuzahlen"}; //label, buy, distributed, paid, sbm_copies.ID, (isbn, price)
    private static final String pdfExportClassOpList[] = {"Vorname", "Nachname", "Geburtstag", "Schüler-ID", "Bücher", "zurück"}; //forename, surname, birth, student_ID
    private static final String pdfExportBookOpList[] = {"Label", "ISBN", "Preis", "Kauf", "ID", "Kopien", "Ausgegeben", "Lager"}; //label, isbn, price, buy, ID
    DefaultListModel pdfExportOpAllesModel = new DefaultListModel();
    DefaultListModel pdfExportOpSelectModel = new DefaultListModel();

    static private String momentaneKopie;
    static private String schuelerId;
    static private int schuelerInKlasse;
    static private int schuelerRow;
    static private TableColumn col;
    static private String momentaneKlasse = null;
    static private int currentPanel = 1;
    static private int speichern = 0;
    static private int studentClassEdit = 0;
    static private double pay;

    static private String user;
    static private int lizenz;
    static private final String[] lizenzenNamen = {"lokaler Admin", "Admin", "Sekretär", "Lehrkraft"};

    Connection conn = null;

    private Thread exp;
    Runnable exportRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                /**
                 * *************************************************
                 */
                if (groupExport.isSelected()) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                    chooser.showSaveDialog(basePanel);

                    String pathName2 = chooser.getSelectedFile().getPath();
                    if (!pathName2.contains(".pdf")) {
                        pathName2 = pathName2.concat(".pdf");
                    }

                    final Document document = new Document(PageSize.A4);

                    final PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pathName2));

                    document.addAuthor(System.getProperty("user.name"));
                    document.addCreationDate();
                    document.addCreator("Seminarkurs Programm Schulbuchverwaltung");
                    document.addTitle("PDF-Export");

                    document.open();
                    for (int i = 0; i < pdfExportAuswahlSelectListModel.size(); i++) {

                        setProgressBarExportString((String) pdfExportAuswahlSelectListModel.getElementAt(i));
                        pdfExportProgressBar.setValue(i);
//*****                    
                        if (schuelerRadioButton.isSelected()) {
                            schuelerId = Students.StudentSearch((String) pdfExportAuswahlSelectListModel.getElementAt(i));
                            document.add(PDF_Export.pdfChapterStudent(schuelerId));
                            document.add(schuelerEx(schuelerId));
                            if (sumPrice.isSelected()) {
                                pay = pay * 100;
                                pay = Math.round(pay);
                                pay = pay / 100;
                                document.add(new Phrase("Gesamt: " + pay + " €", format));
                            }
//*****                        
                        } else if (klasseRadioButton.isSelected()) {
                            String classe = (String) pdfExportAuswahlSelectListModel.getElementAt(i);
                            document.add(PDF_Export.pdfChapterClass(classe));
                            document.add(classEx(classe));
//*****
                        } else if (buchRadioButton.isSelected()) {
                            i = pdfExportAuswahlSelectListModel.size();
                            document.add(PDF_Export.pdfChapterBook());
                            document.add(bookEx());
//*****
                        } else {
                            Other.errorWin("Fatal Error");
                            return;
                        }

                    }
                    document.close();
                    writer.close();
                    /**
                     * *************************************************
                     */
                } else if (soloExport.isSelected()) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                    chooser.showOpenDialog(basePanel);

                    final String pathName2 = chooser.getSelectedFile().getPath();

                    PdfWriter writer;

                    for (int i = 0; i < pdfExportAuswahlSelectListModel.size(); i++) {

                        setProgressBarExportString((String) pdfExportAuswahlSelectListModel.getElementAt(i));
                        pdfExportProgressBar.setValue(i);

                        final Document document = new Document(PageSize.A4);

                        writer = PdfWriter.getInstance(document, new FileOutputStream(pathName2 + "\\" + pdfExportAuswahlSelectListModel.getElementAt(i).toString().replace(" ", "-") + ".pdf"));

                        document.addAuthor(System.getProperty("user.name"));
                        document.addCreationDate();
                        document.addCreator("Seminarkurs Programm Schulbuchverwaltung");
                        document.addTitle("PDF-Export von " + pdfExportAuswahlSelectListModel.getElementAt(i));

                        document.open();
//*****                    
                        if (schuelerRadioButton.isSelected()) {
                            schuelerId = Students.StudentSearch((String) pdfExportAuswahlSelectListModel.getElementAt(i));
                            document.add(PDF_Export.pdfChapterStudent(schuelerId));
                            document.add(schuelerEx(schuelerId));
                            if (sumPrice.isSelected()) {
                                pay = pay * 100;
                                pay = Math.round(pay);
                                pay = pay / 100;
                                document.add(new Phrase("Gesamt: " + pay + " €", format));
                            }
//*****                    
                        } else if (klasseRadioButton.isSelected()) {
                            String classe = (String) pdfExportAuswahlSelectListModel.getElementAt(i);
                            document.add(PDF_Export.pdfChapterClass(classe));
                            document.add(classEx(classe));
//*****                    
                        } else {
                            Other.errorWin("Fatal Error");
                            return;
                        }

                        document.close();
                        writer.close();
                    }
                    /**
                     * *************************************************
                     */
                } else {
                    Other.errorWin("Fatal Error");
                    return;
                }

                pdfExportProgressBar.setValue(pdfExportAuswahlSelectListModel.size());
                setProgressBarExportString("Fertig");//?
                pdfExportPrint.setEnabled(true);

            } catch (DocumentException | FileNotFoundException e) {
                System.out.println(e + " => GroupExport");
                logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
                pdfExportPrint.setEnabled(true);
            }
        }
    };

    private void UpdateTable(ArrayList<String> data) {

        if (currentPanel == 2) {
            while (schuelerModel.getRowCount() != 0) {
                schuelerModel.removeRow(0);
            }

            for (int a = 0; a <= data.size() - 4; a = a + 4) {
                Object[] obj = {data.get(a + 1), data.get(a), data.get(a + 2)};
                schuelerModel.addRow(obj);
            }

            schuelerTbl.setModel(schuelerModel);

            if (schulerColSizeSet) {
                schulerColSizeSet = false;
                for (int i = 0; i < schulerColSize.length; i++) {
                    col = schuelerTbl.getColumnModel().getColumn(i);
                    col.setPreferredWidth(schulerColSize[i]);
                }
                schuelerTbl.setAutoResizeMode(1);
            }
        }

        if (currentPanel == 3) {
            while (buecherModel.getRowCount() != 0) {
                buecherModel.removeRow(0);
            }

            for (int b = 0; b <= data.size() - 4; b = b + 4) {
                Object[] obj = {data.get(b), data.get(b + 1), data.get(b + 2), data.get(b + 3)};
                buecherModel.addRow(obj);
            }
            buecherTbl.setModel(buecherModel);

            if (buecherColSizeSet) {
                buecherColSizeSet = false;
                for (int i = 0; i < buecherColSize.length; i++) {
                    col = buecherTbl.getColumnModel().getColumn(i);
                    col.setPreferredWidth(buecherColSize[i]);
                }
                buecherTbl.setAutoResizeMode(1);
            }
        }
    }

    public Oberflaeche() {
        initComponents();

        //this.setExtendedState(Frame.MAXIMIZED_BOTH);
        schuelerCount.setText(Home.StudentsCount());
        freieBuecher.setText(Home.CauchtCopyCount());

        switch (lizenz) {
            case 3://Lehrkraft
                neuKlasseBtn.setEnabled(false);                 //neue Klasse anlegen
                neuKlasseBtn.setVisible(false);
                neuKlasseFeld.setVisible(false);
                HoverSchuelerHinzufuegen.setEnabled(false);                       //Hover neue Klasse
                HoverSchuelerHinzufuegen.setVisible(false);
                jScrollPane5.setVisible(false);                 //Klassenzugehörigkeit eines Schülers bearbeiten
                jScrollPane8.setVisible(false);
                schuelerKlassenBearbeiten.setEnabled(false);
                schuelerKlassenBearbeiten.setVisible(false);
                HoverSingleSchuelerEdit.setVisible(false);                      //Hover Klassenzugehoerigkeit bearbeiten
                HoverSingleSchuelerEdit.setEnabled(false);
                neuKopieBtn.setEnabled(false);                  //neue Kopien eines Buches erstellen
                neuKopieBtn.setVisible(false);
                HoverSingleBookKopien.setEnabled(false);                      //Hover neue Kopie erstellen
                HoverSingleBookKopien.setVisible(false);
                jLabel18.setVisible(false);
                neuKopieAnzahl.setVisible(false);
                buchNeu.setEnabled(false);                      //neue Bücher erstellen
                buchNeu.setVisible(false);
                HoverSingleBookNeu.setEnabled(false);                      // Hover neues Buch
                HoverSingleBookNeu.setVisible(false);
                kopieBarcodeErneut.setEnabled(false);           //Barcode erneut ausdrucken
                kopieBarcodeErneut.setVisible(false);
                HoverSingleCopieBarcode.setEnabled(false);                       // Hover Barcode erneut ausdrucken
                HoverSingleCopieBarcode.setVisible(false);

            case 2://Sekretär
                buchLöschen.setEnabled(false);                  //Bücher löschen
                buchLöschen.setVisible(false);
                HoverSingleBookLoeschen.setEnabled(false);                      //Hover Buch löschen
                HoverSingleBookLoeschen.setVisible(false);
                buchBearbeiten.setEnabled(false);               //Bücher bearbeiten
                buchBearbeiten.setVisible(false);
                HoverSingleBookBearbeiten.setEnabled(false);                      //Hover Buch bearbeiten
                HoverSingleBookBearbeiten.setVisible(false);
                kopieLöschen.setEnabled(false);                 //Kopien löschen
                kopieLöschen.setVisible(false);
                HoverSingleCopieLoeschen.setEnabled(false);                       //Hover Kopie löschen
                HoverSingleCopieLoeschen.setVisible(false);

            case 1://Admin

            case 0://lokaler Admin    

                break;

            default:

        }

        lizenzName.setText(lizenzenNamen[lizenz]);
        welcome.setText("Willkommen " + user);

        buchAb.setVisible(false);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pdfExportButtonGroup = new javax.swing.ButtonGroup();
        exportFilesGroup = new javax.swing.ButtonGroup();
        basePanel = new javax.swing.JTabbedPane();
        homeTab = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        schuelerCount = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        freieBuecher = new javax.swing.JLabel();
        welcome = new javax.swing.JLabel();
        lizenzName = new javax.swing.JLabel();
        HoverHomeLizenz = new javax.swing.JLabel();
        ToolTip5 = new javax.swing.JLabel();
        schuelerTab = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        klassenList = new javax.swing.JList();
        schuelerTblPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        schuelerTbl = new javax.swing.JTable();
        klasseExportBtn = new javax.swing.JButton();
        neuKlasseFeld = new javax.swing.JTextField();
        neuKlasseBtn = new javax.swing.JButton();
        klasseExportPreislist = new javax.swing.JButton();
        HoverSchuelerExport = new javax.swing.JLabel();
        HoverSchuelerPreisExport = new javax.swing.JLabel();
        HoverSchuelerHinzufuegen = new javax.swing.JLabel();
        ToolTip6 = new javax.swing.JLabel();
        einSchuelerTab = new javax.swing.JPanel();
        schuelerName = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        schuelerGeburt = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        schuelerBuecherTbl = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        schuelerKlassenList = new javax.swing.JList();
        schuelerExport = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        schuelerZurueckAnzahl = new javax.swing.JLabel();
        schuelerZurueck = new javax.swing.JButton();
        schuelerWeiter = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        schuelerKlassenListNeu = new javax.swing.JList();
        schuelerKlassenBearbeiten = new javax.swing.JButton();
        ausgebenIDFeld = new javax.swing.JTextField();
        ausgeben = new javax.swing.JButton();
        ausgebenKaufenFeld = new javax.swing.JTextField();
        buecherSchuelerTblAkt = new javax.swing.JButton();
        schuelerExportPreisliste = new javax.swing.JButton();
        schuelerID = new javax.swing.JLabel();
        ToolTip7 = new javax.swing.JLabel();
        ToolTip8 = new javax.swing.JLabel();
        ToolTip9 = new javax.swing.JLabel();
        HoverSingleSchuelerBezahlt = new javax.swing.JLabel();
        HoverSingleSchuelerAusgeben = new javax.swing.JLabel();
        HoverSingleSchuelerEdit = new javax.swing.JLabel();
        HoverSingleSchuelerVor = new javax.swing.JLabel();
        HoverSingleSchuelerZurueck = new javax.swing.JLabel();
        buecherTab = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        buecherTbl = new javax.swing.JTable();
        buecherTblAkt = new javax.swing.JButton();
        einBuchTab = new javax.swing.JPanel();
        einBuchISBNL = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        isbnSuche = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        labelSuche = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        neuKopieBtn = new javax.swing.JButton();
        neuKopieAnzahl = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        buchNeu = new javax.swing.JButton();
        buchBearbeiten = new javax.swing.JButton();
        buchLöschen = new javax.swing.JButton();
        einBuchISBNFeld = new javax.swing.JTextField();
        einBuchKaufFeld = new javax.swing.JTextField();
        einBuchPreisFeld = new javax.swing.JTextField();
        einBuchLabelFeld = new javax.swing.JTextField();
        HoverSingleBookSucheISBN = new javax.swing.JLabel();
        HoverSingleBookSucheLabel = new javax.swing.JLabel();
        HoverSingleBookKopien = new javax.swing.JLabel();
        HoverSingleBookNeu = new javax.swing.JLabel();
        HoverSingleBookBearbeiten = new javax.swing.JLabel();
        HoverSingleBookLoeschen = new javax.swing.JLabel();
        ToolTip10 = new javax.swing.JLabel();
        ToolTip11 = new javax.swing.JLabel();
        ToolTip12 = new javax.swing.JLabel();
        buchAb = new javax.swing.JButton();
        HoverSingleBookISBN = new javax.swing.JLabel();
        HoverSingleBookKauf = new javax.swing.JLabel();
        HoverSingleBookPreis = new javax.swing.JLabel();
        einKopieTab = new javax.swing.JPanel();
        eineKopieSuchen = new javax.swing.JTextField();
        kopieLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        kopieFore = new javax.swing.JLabel();
        kopieSur = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        kopieDistributed = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        kopieBought = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        kopiePaid = new javax.swing.JLabel();
        kopieEinsammeln = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        kopieBarcodeErneut = new javax.swing.JButton();
        PicEinzelneKopie = new javax.swing.JLabel();
        kopieClass = new javax.swing.JLabel();
        kopieLöschen = new javax.swing.JButton();
        ToolTip1 = new javax.swing.JLabel();
        HoverSingleCopieSuche = new javax.swing.JLabel();
        HoverSingleCopieEinsammeln = new javax.swing.JLabel();
        HoverSingleCopieLoeschen = new javax.swing.JLabel();
        HoverSingleCopieBarcode = new javax.swing.JLabel();
        ToolTip2 = new javax.swing.JLabel();
        ToolTip3 = new javax.swing.JLabel();
        ToolTip4 = new javax.swing.JLabel();
        einsammelnTab = new javax.swing.JPanel();
        einsammelnEingabe = new javax.swing.JTextField();
        einsammelnEintragLoeschen = new javax.swing.JButton();
        einsammelnAlles = new javax.swing.JButton();
        jScrollPane13 = new javax.swing.JScrollPane();
        einsammelnTabelle = new javax.swing.JTable();
        einsammelnPic = new javax.swing.JLabel();
        HoverCollectCode = new javax.swing.JLabel();
        HoverCollectLoeschen = new javax.swing.JLabel();
        HoverCollectEinsammeln = new javax.swing.JLabel();
        ToolTip13 = new javax.swing.JLabel();
        ToolTip14 = new javax.swing.JLabel();
        exportTab = new javax.swing.JPanel();
        klasseRadioButton = new javax.swing.JRadioButton();
        schuelerRadioButton = new javax.swing.JRadioButton();
        buchRadioButton = new javax.swing.JRadioButton();
        jScrollPane12 = new javax.swing.JScrollPane();
        pdfExportAuswahlAllesList = new javax.swing.JList();
        pdfExportAddAllesButton = new javax.swing.JButton();
        pdfExportAddSelectButton = new javax.swing.JButton();
        pdfExportDelAllButton = new javax.swing.JButton();
        jScrollPane14 = new javax.swing.JScrollPane();
        pdfExportAuswahlSelectList = new javax.swing.JList();
        pdfExportDelSelectButton = new javax.swing.JButton();
        superSelectComboBox = new javax.swing.JComboBox();
        jScrollPane17 = new javax.swing.JScrollPane();
        pdfExportOpAllesList = new javax.swing.JList();
        pdfExportOpAddAllesButton = new javax.swing.JButton();
        pdfExportOpAddSelectButton2 = new javax.swing.JButton();
        pdfExportOpDelAllButton2 = new javax.swing.JButton();
        jScrollPane18 = new javax.swing.JScrollPane();
        pdfExportOpSelectList2 = new javax.swing.JList();
        pdfExportOpDelSelectButton2 = new javax.swing.JButton();
        pdfExportPrint = new javax.swing.JButton();
        soloExport = new javax.swing.JRadioButton();
        groupExport = new javax.swing.JRadioButton();
        numCheckBox = new javax.swing.JCheckBox();
        pdfExportProgressBar = new javax.swing.JProgressBar();
        sumPrice = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Schulbuchverwaltung");
        setResizable(false);

        jLabel2.setFont(new java.awt.Font("Arial", 0, 30)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Schulbuch Verwaltung");

        jLabel3.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Anzahl Schüler:");
        jLabel3.setVerifyInputWhenFocusTarget(false);

        schuelerCount.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        schuelerCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        schuelerCount.setText("---");

        jLabel4.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Anzahl freier Bücher:");

        freieBuecher.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        freieBuecher.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        freieBuecher.setText("---");

        welcome.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        welcome.setText("---");

        lizenzName.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        lizenzName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lizenzName.setText("---");

        HoverHomeLizenz.setForeground(new java.awt.Color(0, 0, 255));
        HoverHomeLizenz.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverHomeLizenz.setText("?");
        HoverHomeLizenz.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverHomeLizenzMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverHomeLizenzMouseExited(evt);
            }
        });

        ToolTip5.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout homeTabLayout = new javax.swing.GroupLayout(homeTab);
        homeTab.setLayout(homeTabLayout);
        homeTabLayout.setHorizontalGroup(
            homeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(homeTabLayout.createSequentialGroup()
                .addGroup(homeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(homeTabLayout.createSequentialGroup()
                        .addGap(453, 453, 453)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(homeTabLayout.createSequentialGroup()
                        .addGap(300, 300, 300)
                        .addGroup(homeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(welcome, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(homeTabLayout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(schuelerCount, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(250, 250, 250)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(freieBuecher, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(homeTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(HoverHomeLizenz, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ToolTip5, javax.swing.GroupLayout.PREFERRED_SIZE, 882, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(homeTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lizenzName, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(361, Short.MAX_VALUE))
        );
        homeTabLayout.setVerticalGroup(
            homeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(homeTabLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(welcome, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(148, 148, 148)
                .addGroup(homeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(schuelerCount, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(freieBuecher, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 257, Short.MAX_VALUE)
                .addComponent(lizenzName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(homeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ToolTip5, javax.swing.GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
                    .addComponent(HoverHomeLizenz, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        basePanel.addTab("Home", homeTab);

        schuelerTab.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        schuelerTab.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                schuelerTabComponentAdded(evt);
            }
        });

        klassenList.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        klassenList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                klassenListMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(klassenList);

        schuelerTbl.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        schuelerTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        schuelerTbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                schuelerTblMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(schuelerTbl);

        javax.swing.GroupLayout schuelerTblPanelLayout = new javax.swing.GroupLayout(schuelerTblPanel);
        schuelerTblPanel.setLayout(schuelerTblPanelLayout);
        schuelerTblPanelLayout.setHorizontalGroup(
            schuelerTblPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1044, Short.MAX_VALUE)
        );
        schuelerTblPanelLayout.setVerticalGroup(
            schuelerTblPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(schuelerTblPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 590, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        klasseExportBtn.setText("Klasse als PDF Exportieren");
        klasseExportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                klasseExportBtnActionPerformed(evt);
            }
        });

        neuKlasseBtn.setText("Hinzufügen");
        neuKlasseBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                neuKlasseBtnMouseClicked(evt);
            }
        });

        klasseExportPreislist.setText("Preisliste Klasse Exportieren");
        klasseExportPreislist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                klasseExportPreislistActionPerformed(evt);
            }
        });

        HoverSchuelerExport.setForeground(new java.awt.Color(0, 0, 255));
        HoverSchuelerExport.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSchuelerExport.setText("?");
        HoverSchuelerExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSchuelerExportMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSchuelerExportMouseExited(evt);
            }
        });

        HoverSchuelerPreisExport.setForeground(new java.awt.Color(0, 0, 255));
        HoverSchuelerPreisExport.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSchuelerPreisExport.setText("?");
        HoverSchuelerPreisExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSchuelerPreisExportMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSchuelerPreisExportMouseExited(evt);
            }
        });

        HoverSchuelerHinzufuegen.setForeground(new java.awt.Color(0, 0, 255));
        HoverSchuelerHinzufuegen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSchuelerHinzufuegen.setText("?");
        HoverSchuelerHinzufuegen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSchuelerHinzufuegenMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSchuelerHinzufuegenMouseExited(evt);
            }
        });

        ToolTip6.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout schuelerTabLayout = new javax.swing.GroupLayout(schuelerTab);
        schuelerTab.setLayout(schuelerTabLayout);
        schuelerTabLayout.setHorizontalGroup(
            schuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(schuelerTabLayout.createSequentialGroup()
                .addGroup(schuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(schuelerTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(schuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(neuKlasseFeld)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(schuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(schuelerTblPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(schuelerTabLayout.createSequentialGroup()
                                .addComponent(neuKlasseBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(HoverSchuelerHinzufuegen, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(457, 457, 457)
                                .addComponent(HoverSchuelerExport, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(klasseExportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(HoverSchuelerPreisExport, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(klasseExportPreislist, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(schuelerTabLayout.createSequentialGroup()
                        .addGap(315, 315, 315)
                        .addComponent(ToolTip6, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        schuelerTabLayout.setVerticalGroup(
            schuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(schuelerTabLayout.createSequentialGroup()
                .addGroup(schuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(schuelerTblPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(schuelerTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane3)))
                .addGap(18, 18, 18)
                .addGroup(schuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(schuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(neuKlasseFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(neuKlasseBtn)
                        .addComponent(HoverSchuelerHinzufuegen))
                    .addComponent(klasseExportBtn)
                    .addGroup(schuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(klasseExportPreislist)
                        .addComponent(HoverSchuelerExport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(HoverSchuelerPreisExport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(2, 2, 2)
                .addComponent(ToolTip6, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        basePanel.addTab("Schüler", schuelerTab);

        schuelerName.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        schuelerName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        schuelerName.setText("Name");

        jLabel6.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel6.setText("Geburtsdatum:");

        jLabel7.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel7.setText("Klassen:");

        schuelerGeburt.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        schuelerGeburt.setText("-----");

        schuelerBuecherTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "num", "Title 2", "Title 3", "Title 4", "Title 4", "Title 5"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, true, true, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(schuelerBuecherTbl);
        if (schuelerBuecherTbl.getColumnModel().getColumnCount() > 0) {
            schuelerBuecherTbl.getColumnModel().getColumn(0).setResizable(false);
            schuelerBuecherTbl.getColumnModel().getColumn(0).setPreferredWidth(30);
        }

        schuelerKlassenList.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        schuelerKlassenList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "-----" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        schuelerKlassenList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                schuelerKlassenListMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(schuelerKlassenList);

        schuelerExport.setText("Export als PDF");
        schuelerExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                schuelerExportMouseClicked(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel8.setText("Zurück zu geben:");

        schuelerZurueckAnzahl.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        schuelerZurueckAnzahl.setText("-----");

        schuelerZurueck.setText("<<<");
        schuelerZurueck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                schuelerZurueckActionPerformed(evt);
            }
        });

        schuelerWeiter.setText(">>>");
        schuelerWeiter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                schuelerWeiterActionPerformed(evt);
            }
        });

        schuelerKlassenListNeu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                schuelerKlassenListNeuMouseClicked(evt);
            }
        });
        jScrollPane8.setViewportView(schuelerKlassenListNeu);

        schuelerKlassenBearbeiten.setText("Bearbeiten");
        schuelerKlassenBearbeiten.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                schuelerKlassenBearbeitenActionPerformed(evt);
            }
        });

        ausgebenIDFeld.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ausgebenIDFeldActionPerformed(evt);
            }
        });

        ausgeben.setText("Ausgeben");
        ausgeben.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ausgebenActionPerformed(evt);
            }
        });

        ausgebenKaufenFeld.setText("0");

        buecherSchuelerTblAkt.setText("Aktualisieren");
        buecherSchuelerTblAkt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buecherSchuelerTblAktActionPerformed(evt);
            }
        });

        schuelerExportPreisliste.setText("Preislist Export als PDF");
        schuelerExportPreisliste.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                schuelerExportPreislisteMouseClicked(evt);
            }
        });

        schuelerID.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        schuelerID.setText("ID");

        ToolTip7.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        ToolTip8.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        ToolTip9.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        HoverSingleSchuelerBezahlt.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleSchuelerBezahlt.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleSchuelerBezahlt.setText("?");
        HoverSingleSchuelerBezahlt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleSchuelerBezahltMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleSchuelerBezahltMouseExited(evt);
            }
        });

        HoverSingleSchuelerAusgeben.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleSchuelerAusgeben.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleSchuelerAusgeben.setText("?");
        HoverSingleSchuelerAusgeben.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleSchuelerAusgebenMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleSchuelerAusgebenMouseExited(evt);
            }
        });

        HoverSingleSchuelerEdit.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleSchuelerEdit.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleSchuelerEdit.setText("?");
        HoverSingleSchuelerEdit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleSchuelerEditMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleSchuelerEditMouseExited(evt);
            }
        });

        HoverSingleSchuelerVor.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleSchuelerVor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleSchuelerVor.setText("?");
        HoverSingleSchuelerVor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleSchuelerVorMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleSchuelerVorMouseExited(evt);
            }
        });

        HoverSingleSchuelerZurueck.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleSchuelerZurueck.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleSchuelerZurueck.setText("?");
        HoverSingleSchuelerZurueck.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleSchuelerZurueckMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleSchuelerZurueckMouseExited(evt);
            }
        });

        javax.swing.GroupLayout einSchuelerTabLayout = new javax.swing.GroupLayout(einSchuelerTab);
        einSchuelerTab.setLayout(einSchuelerTabLayout);
        einSchuelerTabLayout.setHorizontalGroup(
            einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einSchuelerTabLayout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addComponent(HoverSingleSchuelerZurueck, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(einSchuelerTabLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(188, 188, 188)
                        .addComponent(schuelerZurueckAnzahl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(einSchuelerTabLayout.createSequentialGroup()
                        .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(schuelerName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einSchuelerTabLayout.createSequentialGroup()
                                    .addComponent(jLabel6)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(schuelerGeburt, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(einSchuelerTabLayout.createSequentialGroup()
                                    .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(einSchuelerTabLayout.createSequentialGroup()
                                            .addComponent(jLabel7)
                                            .addGap(81, 81, 81)
                                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(einSchuelerTabLayout.createSequentialGroup()
                                            .addComponent(schuelerZurueck, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(HoverSingleSchuelerVor, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(einSchuelerTabLayout.createSequentialGroup()
                                            .addGap(14, 14, 14)
                                            .addComponent(HoverSingleSchuelerEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(schuelerKlassenBearbeiten, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jScrollPane5)))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einSchuelerTabLayout.createSequentialGroup()
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(schuelerWeiter, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addComponent(schuelerID))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 814, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(einSchuelerTabLayout.createSequentialGroup()
                .addGap(108, 108, 108)
                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(einSchuelerTabLayout.createSequentialGroup()
                        .addComponent(ToolTip7, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(einSchuelerTabLayout.createSequentialGroup()
                        .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ToolTip8, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ToolTip9, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(102, 102, 102)
                        .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(einSchuelerTabLayout.createSequentialGroup()
                                .addComponent(schuelerExport, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(schuelerExportPreisliste, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(HoverSingleSchuelerAusgeben, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ausgeben, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(ausgebenIDFeld, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(16, 16, 16))
                            .addGroup(einSchuelerTabLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einSchuelerTabLayout.createSequentialGroup()
                                        .addComponent(buecherSchuelerTblAkt)
                                        .addGap(344, 344, 344))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einSchuelerTabLayout.createSequentialGroup()
                                        .addComponent(HoverSingleSchuelerBezahlt, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ausgebenKaufenFeld, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(46, 46, 46))))))))
        );
        einSchuelerTabLayout.setVerticalGroup(
            einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(einSchuelerTabLayout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(einSchuelerTabLayout.createSequentialGroup()
                        .addComponent(schuelerName, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(schuelerID)
                        .addGap(18, 18, 18)
                        .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(schuelerGeburt, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(schuelerZurueckAnzahl, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(einSchuelerTabLayout.createSequentialGroup()
                                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(schuelerKlassenBearbeiten)
                                    .addComponent(HoverSingleSchuelerEdit))
                                .addGap(118, 118, 118)
                                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(schuelerZurueck)
                                    .addComponent(schuelerWeiter)
                                    .addComponent(HoverSingleSchuelerVor)
                                    .addComponent(HoverSingleSchuelerZurueck)))
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buecherSchuelerTblAkt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(einSchuelerTabLayout.createSequentialGroup()
                        .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(schuelerExport)
                            .addComponent(ausgeben)
                            .addComponent(schuelerExportPreisliste)
                            .addComponent(ausgebenIDFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(HoverSingleSchuelerAusgeben))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ausgebenKaufenFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(HoverSingleSchuelerBezahlt))
                        .addGap(39, 39, 39))
                    .addGroup(einSchuelerTabLayout.createSequentialGroup()
                        .addComponent(ToolTip7, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ToolTip8, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ToolTip9, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(53, 53, 53))))
        );

        basePanel.addTab("Einzelner Schueler", einSchuelerTab);

        buecherTab.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                buecherTabComponentAdded(evt);
            }
        });

        buecherTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        buecherTbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                buecherTblMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(buecherTbl);

        buecherTblAkt.setText("Aktualisieren");
        buecherTblAkt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buecherTblAktActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buecherTabLayout = new javax.swing.GroupLayout(buecherTab);
        buecherTab.setLayout(buecherTabLayout);
        buecherTabLayout.setHorizontalGroup(
            buecherTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buecherTabLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buecherTblAkt)
                .addGap(601, 601, 601))
        );
        buecherTabLayout.setVerticalGroup(
            buecherTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buecherTabLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 630, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buecherTblAkt)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        basePanel.addTab("Bücher", buecherTab);

        einBuchISBNL.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        einBuchISBNL.setText("ISBN:");

        jLabel11.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel11.setText("Kaufbuch:");

        jLabel14.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel14.setText("Preis:");

        isbnSuche.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        isbnSuche.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isbnSucheActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel16.setText("Nach ISBN suchen");

        labelSuche.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        labelSuche.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelSucheActionPerformed(evt);
            }
        });

        jLabel17.setText("Nach Buchlabel suchen");

        neuKopieBtn.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        neuKopieBtn.setText("Neue Kopie erstellen");
        neuKopieBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                neuKopieBtnMouseClicked(evt);
            }
        });

        jLabel18.setText("Anzahl an neuen Kopien");

        buchNeu.setText("Neues Buch");
        buchNeu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buchNeuActionPerformed(evt);
            }
        });

        buchBearbeiten.setText("Buch bearbeiten");
        buchBearbeiten.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buchBearbeitenActionPerformed(evt);
            }
        });

        buchLöschen.setText("Buch löschen");
        buchLöschen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buchLöschenActionPerformed(evt);
            }
        });

        einBuchISBNFeld.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        einBuchISBNFeld.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        einBuchKaufFeld.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        einBuchKaufFeld.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        einBuchPreisFeld.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        einBuchPreisFeld.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        einBuchLabelFeld.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        einBuchLabelFeld.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        einBuchLabelFeld.setText("- - -");

        HoverSingleBookSucheISBN.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleBookSucheISBN.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleBookSucheISBN.setText("?");
        HoverSingleBookSucheISBN.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleBookSucheISBNMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleBookSucheISBNMouseExited(evt);
            }
        });

        HoverSingleBookSucheLabel.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleBookSucheLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleBookSucheLabel.setText("?");
        HoverSingleBookSucheLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleBookSucheLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleBookSucheLabelMouseExited(evt);
            }
        });

        HoverSingleBookKopien.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleBookKopien.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleBookKopien.setText("?");
        HoverSingleBookKopien.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleBookKopienMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleBookKopienMouseExited(evt);
            }
        });

        HoverSingleBookNeu.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleBookNeu.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleBookNeu.setText("?");
        HoverSingleBookNeu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleBookNeuMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleBookNeuMouseExited(evt);
            }
        });

        HoverSingleBookBearbeiten.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleBookBearbeiten.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleBookBearbeiten.setText("?");
        HoverSingleBookBearbeiten.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleBookBearbeitenMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleBookBearbeitenMouseExited(evt);
            }
        });

        HoverSingleBookLoeschen.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleBookLoeschen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleBookLoeschen.setText("?");
        HoverSingleBookLoeschen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleBookLoeschenMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleBookLoeschenMouseExited(evt);
            }
        });

        ToolTip10.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        ToolTip11.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        ToolTip12.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        buchAb.setText("Abbrechen");
        buchAb.setEnabled(false);
        buchAb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buchAbActionPerformed(evt);
            }
        });

        HoverSingleBookISBN.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleBookISBN.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleBookISBN.setText("?");
        HoverSingleBookISBN.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleBookISBNMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleBookISBNMouseExited(evt);
            }
        });

        HoverSingleBookKauf.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleBookKauf.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleBookKauf.setText("?");
        HoverSingleBookKauf.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleBookKaufMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleBookKaufMouseExited(evt);
            }
        });

        HoverSingleBookPreis.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleBookPreis.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleBookPreis.setText("?");
        HoverSingleBookPreis.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleBookPreisMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleBookPreisMouseExited(evt);
            }
        });

        javax.swing.GroupLayout einBuchTabLayout = new javax.swing.GroupLayout(einBuchTab);
        einBuchTab.setLayout(einBuchTabLayout);
        einBuchTabLayout.setHorizontalGroup(
            einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(einBuchTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(einBuchTabLayout.createSequentialGroup()
                        .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addGroup(einBuchTabLayout.createSequentialGroup()
                                .addComponent(isbnSuche, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(HoverSingleBookSucheISBN, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel17))
                        .addGap(457, 1141, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einBuchTabLayout.createSequentialGroup()
                        .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, einBuchTabLayout.createSequentialGroup()
                                .addComponent(labelSuche, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(HoverSingleBookSucheLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(238, 238, 238)
                                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(einBuchTabLayout.createSequentialGroup()
                                        .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(einBuchISBNL, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 267, Short.MAX_VALUE))
                                    .addComponent(einBuchLabelFeld)))
                            .addGroup(einBuchTabLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(einBuchTabLayout.createSequentialGroup()
                                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(neuKopieAnzahl, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(neuKopieBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(einBuchTabLayout.createSequentialGroup()
                                        .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(HoverSingleBookISBN, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(HoverSingleBookKauf, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(HoverSingleBookPreis, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(einBuchPreisFeld, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(einBuchKaufFeld, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(einBuchISBNFeld, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))))))
                        .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(einBuchTabLayout.createSequentialGroup()
                                        .addGap(343, 343, 343)
                                        .addComponent(HoverSingleBookBearbeiten, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(buchBearbeiten))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einBuchTabLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(HoverSingleBookLoeschen, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(buchLöschen, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einBuchTabLayout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(HoverSingleBookNeu, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buchNeu, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(einBuchTabLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(HoverSingleBookKopien, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
            .addGroup(einBuchTabLayout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ToolTip12, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ToolTip11, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ToolTip10, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einBuchTabLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buchAb)
                .addContainerGap())
        );
        einBuchTabLayout.setVerticalGroup(
            einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(einBuchTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isbnSuche, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HoverSingleBookSucheISBN))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addGap(12, 12, 12)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSuche, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(einBuchLabelFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HoverSingleBookSucheLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel17)
                .addGap(41, 41, 41)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(einBuchISBNL)
                    .addComponent(buchNeu)
                    .addComponent(einBuchISBNFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HoverSingleBookNeu)
                    .addComponent(HoverSingleBookISBN))
                .addGap(41, 41, 41)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(buchBearbeiten)
                    .addComponent(einBuchKaufFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HoverSingleBookBearbeiten)
                    .addComponent(HoverSingleBookKauf))
                .addGap(41, 41, 41)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(buchLöschen)
                    .addComponent(einBuchPreisFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HoverSingleBookLoeschen)
                    .addComponent(HoverSingleBookPreis))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buchAb)
                .addGap(78, 78, 78)
                .addComponent(ToolTip10, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ToolTip11, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ToolTip12, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(68, 68, 68)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(neuKopieBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HoverSingleBookKopien))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(neuKopieAnzahl, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(91, 91, 91))
        );

        basePanel.addTab("Einzelnes Buch", einBuchTab);

        einKopieTab.setPreferredSize(new java.awt.Dimension(1373, 672));

        eineKopieSuchen.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        eineKopieSuchen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eineKopieSuchenActionPerformed(evt);
            }
        });

        kopieLabel.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        kopieLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        kopieLabel.setText("---");

        jLabel5.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel5.setText("Ausgegeben an:");

        kopieFore.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        kopieFore.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);

        kopieSur.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel9.setText("Ausgegeben am:");

        kopieDistributed.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        kopieDistributed.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);

        jLabel10.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel10.setText("Gekauft:");

        kopieBought.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        kopieBought.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);

        jLabel12.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel12.setText("Bezahlt:");

        kopiePaid.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        kopiePaid.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);

        kopieEinsammeln.setText("Einsammeln");
        kopieEinsammeln.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                kopieEinsammelnMouseClicked(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabel15.setText("Nach Kopie suchen");

        kopieBarcodeErneut.setLabel("Barcode nochmal drucken");
        kopieBarcodeErneut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kopieBarcodeErneutActionPerformed(evt);
            }
        });

        PicEinzelneKopie.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sbv/pictures/missingPicture.png"))); // NOI18N

        kopieClass.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N

        kopieLöschen.setText("Löschen");
        kopieLöschen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kopieLöschenActionPerformed(evt);
            }
        });

        ToolTip1.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        HoverSingleCopieSuche.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleCopieSuche.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleCopieSuche.setText("?");
        HoverSingleCopieSuche.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleCopieSucheMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleCopieSucheMouseExited(evt);
            }
        });

        HoverSingleCopieEinsammeln.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleCopieEinsammeln.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleCopieEinsammeln.setText("?");
        HoverSingleCopieEinsammeln.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleCopieEinsammelnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleCopieEinsammelnMouseExited(evt);
            }
        });

        HoverSingleCopieLoeschen.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleCopieLoeschen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleCopieLoeschen.setText("?");
        HoverSingleCopieLoeschen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleCopieLoeschenMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleCopieLoeschenMouseExited(evt);
            }
        });

        HoverSingleCopieBarcode.setForeground(new java.awt.Color(0, 0, 255));
        HoverSingleCopieBarcode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverSingleCopieBarcode.setText("?");
        HoverSingleCopieBarcode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverSingleCopieBarcodeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverSingleCopieBarcodeMouseExited(evt);
            }
        });

        ToolTip2.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        ToolTip3.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        ToolTip4.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout einKopieTabLayout = new javax.swing.GroupLayout(einKopieTab);
        einKopieTab.setLayout(einKopieTabLayout);
        einKopieTabLayout.setHorizontalGroup(
            einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(einKopieTabLayout.createSequentialGroup()
                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(einKopieTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(einKopieTabLayout.createSequentialGroup()
                                .addComponent(eineKopieSuchen, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(HoverSingleCopieSuche, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(einKopieTabLayout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(einKopieTabLayout.createSequentialGroup()
                                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(einKopieTabLayout.createSequentialGroup()
                                        .addGap(453, 453, 453)
                                        .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(HoverSingleCopieEinsammeln, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(HoverSingleCopieLoeschen, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(ToolTip2, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ToolTip3, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ToolTip4, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ToolTip1, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(kopieLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(einKopieTabLayout.createSequentialGroup()
                                        .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(kopieEinsammeln, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(einKopieTabLayout.createSequentialGroup()
                                                .addComponent(kopieLöschen)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(HoverSingleCopieBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(kopieBarcodeErneut))
                                            .addGroup(einKopieTabLayout.createSequentialGroup()
                                                .addGap(58, 58, 58)
                                                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(kopiePaid, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(kopieDistributed, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                                        .addComponent(kopieBought, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                                        .addGap(18, 18, 18)
                                        .addComponent(PicEinzelneKopie))))
                            .addGroup(einKopieTabLayout.createSequentialGroup()
                                .addGap(359, 359, 359)
                                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(einKopieTabLayout.createSequentialGroup()
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(33, 33, 33)
                                        .addComponent(kopieFore, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(kopieSur, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(einKopieTabLayout.createSequentialGroup()
                                        .addGap(282, 282, 282)
                                        .addComponent(kopieClass, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(364, 364, 364)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        einKopieTabLayout.setVerticalGroup(
            einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(einKopieTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eineKopieSuchen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HoverSingleCopieSuche))
                .addGap(3, 3, 3)
                .addComponent(jLabel15)
                .addGap(1, 1, 1)
                .addComponent(kopieLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(einKopieTabLayout.createSequentialGroup()
                        .addGap(152, 152, 152)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(kopieEinsammeln, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(HoverSingleCopieEinsammeln))
                        .addGap(18, 18, 18)
                        .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(kopieLöschen)
                            .addComponent(kopieBarcodeErneut)
                            .addComponent(HoverSingleCopieLoeschen)
                            .addComponent(HoverSingleCopieBarcode))
                        .addGap(179, 179, 179))
                    .addGroup(einKopieTabLayout.createSequentialGroup()
                        .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(PicEinzelneKopie)
                            .addGroup(einKopieTabLayout.createSequentialGroup()
                                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(kopieFore, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(kopieSur, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(kopieClass, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(25, 25, 25)
                                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(kopieDistributed, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel9))
                                .addGap(30, 30, 30)
                                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(kopieBought, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel10))
                                .addGap(30, 30, 30)
                                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addComponent(kopiePaid, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(45, 45, 45)
                                .addComponent(ToolTip1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(ToolTip2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(ToolTip3, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(ToolTip4, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(92, Short.MAX_VALUE))))
        );

        basePanel.addTab("Einzelne Kopie", einKopieTab);

        einsammelnEingabe.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        einsammelnEingabe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                einsammelnEingabeActionPerformed(evt);
            }
        });

        einsammelnEintragLoeschen.setEnabled(false);
        einsammelnEintragLoeschen.setLabel("Eintrag löschen");
        einsammelnEintragLoeschen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                einsammelnEintragLoeschenActionPerformed(evt);
            }
        });

        einsammelnAlles.setText("Einsammeln");
        einsammelnAlles.setEnabled(false);
        einsammelnAlles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                einsammelnAllesActionPerformed(evt);
            }
        });

        einsammelnTabelle.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "N"
            }
        ));
        einsammelnTabelle.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        einsammelnTabelle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                einsammelnTabelleMouseClicked(evt);
            }
        });
        jScrollPane13.setViewportView(einsammelnTabelle);

        einsammelnPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sbv/pictures/missingPicture.png"))); // NOI18N

        HoverCollectCode.setForeground(new java.awt.Color(0, 0, 255));
        HoverCollectCode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverCollectCode.setText("?");
        HoverCollectCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverCollectCodeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverCollectCodeMouseExited(evt);
            }
        });

        HoverCollectLoeschen.setForeground(new java.awt.Color(0, 0, 255));
        HoverCollectLoeschen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverCollectLoeschen.setText("?");
        HoverCollectLoeschen.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverCollectLoeschenMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverCollectLoeschenMouseExited(evt);
            }
        });

        HoverCollectEinsammeln.setForeground(new java.awt.Color(0, 0, 255));
        HoverCollectEinsammeln.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HoverCollectEinsammeln.setText("?");
        HoverCollectEinsammeln.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                HoverCollectEinsammelnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                HoverCollectEinsammelnMouseExited(evt);
            }
        });

        ToolTip13.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        ToolTip14.setForeground(new java.awt.Color(0, 0, 255));
        ToolTip14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout einsammelnTabLayout = new javax.swing.GroupLayout(einsammelnTab);
        einsammelnTab.setLayout(einsammelnTabLayout);
        einsammelnTabLayout.setHorizontalGroup(
            einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(einsammelnTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(einsammelnTabLayout.createSequentialGroup()
                        .addComponent(einsammelnEingabe, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(HoverCollectCode, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(HoverCollectLoeschen, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(einsammelnEintragLoeschen, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(einsammelnTabLayout.createSequentialGroup()
                        .addGroup(einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 935, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(einsammelnTabLayout.createSequentialGroup()
                                .addComponent(einsammelnAlles, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(HoverCollectEinsammeln, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addGroup(einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(einsammelnPic, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einsammelnTabLayout.createSequentialGroup()
                                .addGroup(einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ToolTip14, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ToolTip13, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(52, 52, 52)))))
                .addContainerGap())
        );
        einsammelnTabLayout.setVerticalGroup(
            einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einsammelnTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(einsammelnEingabe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(HoverCollectCode))
                    .addGroup(einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(einsammelnEintragLoeschen, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(HoverCollectLoeschen)))
                .addGap(18, 18, 18)
                .addGroup(einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(einsammelnTabLayout.createSequentialGroup()
                        .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 554, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(einsammelnAlles, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(HoverCollectEinsammeln)))
                    .addGroup(einsammelnTabLayout.createSequentialGroup()
                        .addComponent(einsammelnPic)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ToolTip13, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ToolTip14, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        basePanel.addTab("Einsammeln", einsammelnTab);

        pdfExportButtonGroup.add(klasseRadioButton);
        klasseRadioButton.setText("Klasse");
        klasseRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                klasseRadioButtonActionPerformed(evt);
            }
        });

        pdfExportButtonGroup.add(schuelerRadioButton);
        schuelerRadioButton.setText("Schüler");
        schuelerRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                schuelerRadioButtonActionPerformed(evt);
            }
        });

        pdfExportButtonGroup.add(buchRadioButton);
        buchRadioButton.setText("Buch");
        buchRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buchRadioButtonActionPerformed(evt);
            }
        });

        jScrollPane12.setViewportView(pdfExportAuswahlAllesList);

        pdfExportAddAllesButton.setText(">>");
        pdfExportAddAllesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfExportAddAllesButtonActionPerformed(evt);
            }
        });

        pdfExportAddSelectButton.setText(">");
        pdfExportAddSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfExportAddSelectButtonActionPerformed(evt);
            }
        });

        pdfExportDelAllButton.setText("<<");
        pdfExportDelAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfExportDelAllButtonActionPerformed(evt);
            }
        });

        jScrollPane14.setViewportView(pdfExportAuswahlSelectList);

        pdfExportDelSelectButton.setText("<");
        pdfExportDelSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfExportDelSelectButtonActionPerformed(evt);
            }
        });

        superSelectComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                superSelectComboBoxActionPerformed(evt);
            }
        });

        jScrollPane17.setViewportView(pdfExportOpAllesList);

        pdfExportOpAddAllesButton.setText(">>");
        pdfExportOpAddAllesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfExportOpAddAllesButtonActionPerformed(evt);
            }
        });

        pdfExportOpAddSelectButton2.setText(">");
        pdfExportOpAddSelectButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfExportOpAddSelectButton2ActionPerformed(evt);
            }
        });

        pdfExportOpDelAllButton2.setText("<<");
        pdfExportOpDelAllButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfExportOpDelAllButton2ActionPerformed(evt);
            }
        });

        jScrollPane18.setViewportView(pdfExportOpSelectList2);

        pdfExportOpDelSelectButton2.setText("<");
        pdfExportOpDelSelectButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfExportOpDelSelectButton2ActionPerformed(evt);
            }
        });

        pdfExportPrint.setText("PDF erstellen");
        pdfExportPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfExportPrintActionPerformed(evt);
            }
        });

        exportFilesGroup.add(soloExport);
        soloExport.setText("einzeln");

        exportFilesGroup.add(groupExport);
        groupExport.setSelected(true);
        groupExport.setText("zusammen");

        numCheckBox.setSelected(true);
        numCheckBox.setText("Nummerierung");

        pdfExportProgressBar.setMaximum(10);
        pdfExportProgressBar.setToolTipText("");
        pdfExportProgressBar.setStringPainted(true);

        sumPrice.setText("Gesammtkosten");
        sumPrice.setActionCommand("");
        sumPrice.setEnabled(false);

        javax.swing.GroupLayout exportTabLayout = new javax.swing.GroupLayout(exportTab);
        exportTab.setLayout(exportTabLayout);
        exportTabLayout.setHorizontalGroup(
            exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(exportTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(exportTabLayout.createSequentialGroup()
                        .addComponent(klasseRadioButton)
                        .addGap(18, 18, 18)
                        .addComponent(schuelerRadioButton)
                        .addGap(18, 18, 18)
                        .addComponent(buchRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(groupExport)
                        .addGap(18, 18, 18)
                        .addComponent(soloExport)
                        .addGap(18, 18, 18)
                        .addComponent(pdfExportPrint))
                    .addGroup(exportTabLayout.createSequentialGroup()
                        .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(exportTabLayout.createSequentialGroup()
                                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(pdfExportAddAllesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pdfExportDelAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pdfExportAddSelectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pdfExportDelSelectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(superSelectComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(48, 48, 48)
                        .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(exportTabLayout.createSequentialGroup()
                                .addComponent(numCheckBox)
                                .addGap(18, 18, 18)
                                .addComponent(sumPrice)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 484, Short.MAX_VALUE)
                                .addComponent(pdfExportProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(exportTabLayout.createSequentialGroup()
                                .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(pdfExportOpAddAllesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pdfExportOpDelAllButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pdfExportOpAddSelectButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pdfExportOpDelSelectButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        exportTabLayout.setVerticalGroup(
            exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(exportTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(klasseRadioButton)
                    .addComponent(schuelerRadioButton)
                    .addComponent(buchRadioButton)
                    .addComponent(pdfExportPrint)
                    .addComponent(soloExport)
                    .addComponent(groupExport))
                .addGap(25, 25, 25)
                .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(superSelectComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numCheckBox)
                    .addComponent(sumPrice))
                .addGap(18, 18, 18)
                .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane14, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane12, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane18, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane17, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 567, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, exportTabLayout.createSequentialGroup()
                        .addGap(182, 182, 182)
                        .addComponent(pdfExportProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, exportTabLayout.createSequentialGroup()
                        .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, exportTabLayout.createSequentialGroup()
                                .addComponent(pdfExportAddAllesButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pdfExportAddSelectButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pdfExportDelSelectButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pdfExportDelAllButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, exportTabLayout.createSequentialGroup()
                                .addComponent(pdfExportOpAddAllesButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pdfExportOpAddSelectButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pdfExportOpDelSelectButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pdfExportOpDelAllButton2)))
                        .addGap(230, 230, 230)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        basePanel.addTab("PDF Export", exportTab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(basePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 1378, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(basePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void schuelerTabComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_schuelerTabComponentAdded
        ArrayList<String> names = Classes.getClassNameList();
        klassenList.setListData(names.toArray());
        schuelerKlassenListNeu.setListData(names.toArray());
        schuelerKlassenListNeu.setEnabled(false);
    }//GEN-LAST:event_schuelerTabComponentAdded

    private void klassenListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_klassenListMouseClicked
        currentPanel = 2;
        int index = klassenList.locationToIndex(evt.getPoint());
        momentaneKlasse = klassenList.getModel().getElementAt(index).toString();
        klassenList.ensureIndexIsVisible(index);
        ArrayList<String> klasse = Classes.classList(momentaneKlasse);
        UpdateTable(klasse);
        schuelerInKlasse = klasse.size() / 4;
    }//GEN-LAST:event_klassenListMouseClicked

    private void schuelerTblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_schuelerTblMouseClicked
        schuelerRow = schuelerTbl.getSelectedRow();
        schuelerId = Classes.classList(momentaneKlasse).get(schuelerRow * 4 + 3);
        schuelerName.setText(Students.SingelStudent(schuelerId, 1) + " " + Students.SingelStudent(schuelerId, 2));
        schuelerID.setText(Students.SingelStudent(schuelerId, 0));
        schuelerGeburt.setText(Students.SingelStudent(schuelerId, 3));
        schuelerZurueckAnzahl.setText(Students.CopiesToReturn(schuelerId));
        schuelerKlassenList.setListData(Students.SingelStudentClasses(schuelerId).toArray());

        ArrayList<String> names = Students.BookList(schuelerId);

        while (schuelerBuecherModel.getRowCount() != 0) {
            schuelerBuecherModel.removeRow(0);
        }

        for (int i = 0; i <= names.size() - 5; i = i + 5) {
            Object[] obj = {i / 5 + 1, names.get(i), names.get(i + 1), Other.dateToNormal(names.get(i + 2)), names.get(i + 3), names.get(i + 4)};
            schuelerBuecherModel.addRow(obj);
        }

        schuelerBuecherTbl.setModel(schuelerBuecherModel);

        if (schuelerBuecherColSizeSet) {
            schuelerBuecherColSizeSet = false;
            for (int i = 0; i < schuelerBuecherColSize.length; i++) {
                col = schuelerBuecherTbl.getColumnModel().getColumn(i);
                col.setPreferredWidth(schuelerBuecherColSize[i]);
            }
            schuelerBuecherTbl.setAutoResizeMode(1);
        }

        if (evt.getClickCount() == 2) {
            basePanel.setSelectedIndex(2);
        }

        if (schuelerRow == 0) {
            schuelerZurueck.setEnabled(false);
        } else {
            schuelerZurueck.setEnabled(true);
        }

        if (schuelerRow == schuelerInKlasse - 1) {
            schuelerWeiter.setEnabled(false);
        } else {
            schuelerWeiter.setEnabled(true);
        }

    }//GEN-LAST:event_schuelerTblMouseClicked

    private void schuelerExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_schuelerExportMouseClicked
        basePanel.setSelectedIndex(9);

        schuelerRadioButton.setSelected(true);
        schuelerRadioButtonActionPerformed(null);

        superSelectComboBox.setSelectedItem(schuelerKlassenList.getModel().getElementAt(0));
        superSelectComboBoxActionPerformed(null);

        pdfExportAuswahlSelectListModel.addElement(schuelerName.getText());
        pdfExportAuswahlSelectList.setModel(pdfExportAuswahlSelectListModel);

        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[0]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[2]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[5]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[4]);
        pdfExportOpSelectList2.setModel(pdfExportOpSelectModel);

        sumPrice.setEnabled(false);
        sumPrice.setSelected(false);

        numCheckBox.setSelected(true);
    }//GEN-LAST:event_schuelerExportMouseClicked

    private void schuelerWeiterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schuelerWeiterActionPerformed
        if (schuelerRow == 0) {
            schuelerZurueck.setEnabled(true);
        }

        schuelerRow = schuelerRow + 1;
        schuelerId = Classes.classList(momentaneKlasse).get(schuelerRow * 4 + 3);
        schuelerName.setText(Students.SingelStudent(schuelerId, 1) + " " + Students.SingelStudent(schuelerId, 2));
        schuelerID.setText(Students.SingelStudent(schuelerId, 0));
        schuelerGeburt.setText(Students.SingelStudent(schuelerId, 3));
        schuelerZurueckAnzahl.setText(Students.CopiesToReturn(schuelerId));
        schuelerKlassenList.setListData(Students.SingelStudentClasses(schuelerId).toArray());

        ArrayList<String> names = Students.BookList(schuelerId);

        while (schuelerBuecherModel.getRowCount() != 0) {
            schuelerBuecherModel.removeRow(0);
        }

        for (int i = 0; i <= names.size() - 5; i = i + 5) {
            Object[] obj = {i / 5 + 1, names.get(i), names.get(i + 1), Other.dateToNormal(names.get(i + 2)), names.get(i + 3), names.get(i + 4)};
            schuelerBuecherModel.addRow(obj);
        }
        schuelerBuecherTbl.setModel(schuelerBuecherModel);

        if (schuelerRow == schuelerInKlasse - 1) {
            schuelerWeiter.setEnabled(false);
        }
    }//GEN-LAST:event_schuelerWeiterActionPerformed

    private void schuelerZurueckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schuelerZurueckActionPerformed
        if (schuelerRow == schuelerInKlasse - 1) {
            schuelerWeiter.setEnabled(true);
        }

        schuelerRow = schuelerRow - 1;
        schuelerId = Classes.classList(momentaneKlasse).get(schuelerRow * 4 + 3);
        schuelerName.setText(Students.SingelStudent(schuelerId, 1) + " " + Students.SingelStudent(schuelerId, 2));
        schuelerID.setText(Students.SingelStudent(schuelerId, 0));
        schuelerGeburt.setText(Students.SingelStudent(schuelerId, 3));
        schuelerZurueckAnzahl.setText(Students.CopiesToReturn(schuelerId));
        schuelerKlassenList.setListData(Students.SingelStudentClasses(schuelerId).toArray());

        ArrayList<String> names = Students.BookList(schuelerId);

        while (schuelerBuecherModel.getRowCount() != 0) {
            schuelerBuecherModel.removeRow(0);
        }

        for (int i = 0; i <= names.size() - 5; i = i + 5) {
            Object[] obj = {i / 5 + 1, names.get(i), names.get(i + 1), Other.dateToNormal(names.get(i + 2)), names.get(i + 3), names.get(i + 4)};
            schuelerBuecherModel.addRow(obj);
        }
        schuelerBuecherTbl.setModel(schuelerBuecherModel);

        if (schuelerRow == 0) {
            schuelerZurueck.setEnabled(false);
        }
    }//GEN-LAST:event_schuelerZurueckActionPerformed

    private void buecherTabComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_buecherTabComponentAdded
        currentPanel = 3;
        UpdateTable(Books.BookList());
    }//GEN-LAST:event_buecherTabComponentAdded

    private void klasseExportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_klasseExportBtnActionPerformed
        basePanel.setSelectedIndex(9);

        schuelerRadioButton.setSelected(true);
        schuelerRadioButtonActionPerformed(null);

        superSelectComboBox.setSelectedItem(momentaneKlasse);
        superSelectComboBoxActionPerformed(null);

        pdfExportAddAllesButtonActionPerformed(evt);

        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[0]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[2]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[5]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[4]);
        pdfExportOpSelectList2.setModel(pdfExportOpSelectModel);

        sumPrice.setEnabled(false);
        sumPrice.setSelected(false);

        numCheckBox.setSelected(true);
    }//GEN-LAST:event_klasseExportBtnActionPerformed

    private void isbnSucheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isbnSucheActionPerformed
        ArrayList<String> buch = Books.singleBook("%" + isbnSuche.getText() + "%", 0);
        if (buch.isEmpty() == true) {
            einBuchLabelFeld.setText("Kein Buch mit dieser ISBN");
            einBuchISBNFeld.setText("");
            einBuchKaufFeld.setText("");
            einBuchPreisFeld.setText("");
        } else {
            einBuchLabelFeld.setText(buch.get(0));
            einBuchISBNFeld.setText(buch.get(1));
            einBuchKaufFeld.setText(buch.get(3));
            einBuchPreisFeld.setText(buch.get(2));
        }
    }//GEN-LAST:event_isbnSucheActionPerformed

    private void buecherTblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buecherTblMouseClicked
        String buchISBN = Books.BookList().get(buecherTbl.getSelectedRow() * 4 + 1);
        einBuchLabelFeld.setText(Books.singleBook(buchISBN, 0).get(0));
        einBuchISBNFeld.setText(Books.singleBook(buchISBN, 0).get(1));
        einBuchKaufFeld.setText(Books.singleBook(buchISBN, 0).get(3));
        einBuchPreisFeld.setText(Books.singleBook(buchISBN, 0).get(2));

        if (evt.getClickCount() == 2) {
            basePanel.setSelectedIndex(4);
        }
    }//GEN-LAST:event_buecherTblMouseClicked

    private void labelSucheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelSucheActionPerformed
        ArrayList<String> buch = Books.singleBook("%" + labelSuche.getText() + "%", 1);
        if (buch.isEmpty() == true) {
            einBuchLabelFeld.setText("Kein Buch mir diesem Label");
            einBuchISBNFeld.setText("");
            einBuchKaufFeld.setText("");
            einBuchPreisFeld.setText("");
        } else {
            einBuchLabelFeld.setText(buch.get(0));
            einBuchISBNFeld.setText(buch.get(1));
            einBuchKaufFeld.setText(buch.get(3));
            einBuchPreisFeld.setText(buch.get(2));
        }
    }//GEN-LAST:event_labelSucheActionPerformed

    private void neuKopieBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_neuKopieBtnMouseClicked
        int anz = Integer.parseInt(neuKopieAnzahl.getText());
        int id = Copies.newID();
        if (id == 0) {
            Other.errorWin("Fehler beim Erstellen neuer Kopien");
        } else {
            try {
                PDF_Export.barcodePDF(id, anz);
            } catch (IOException | DocumentException e) {
                System.out.println(e + " => barcodePDF");
                logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
            }
            for (int i = 0; i < anz; i++) {
                Copies.addCopy(Books.singleBook(einBuchISBNFeld.getText(), 0).get(4), id + i);
            }
        }
    }//GEN-LAST:event_neuKopieBtnMouseClicked

    private void schuelerKlassenListNeuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_schuelerKlassenListNeuMouseClicked
        if (studentClassEdit == 1) {
            int index = schuelerKlassenListNeu.locationToIndex(evt.getPoint());
            schuelerKlassenListNeu.ensureIndexIsVisible(index);
            Students.addToClass(schuelerId, schuelerKlassenListNeu.getModel().getElementAt(index).toString());
        }
        schuelerKlassenList.setListData(Students.SingelStudentClasses(schuelerId).toArray());
    }//GEN-LAST:event_schuelerKlassenListNeuMouseClicked

    private void buchNeuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buchNeuActionPerformed
        if (speichern == 0) {
            einBuchLabelFeld.setText("");
            einBuchISBNFeld.setText("");
            einBuchKaufFeld.setText("");
            einBuchPreisFeld.setText("");
            buchNeu.setText("Speichern");
            buchBearbeiten.setEnabled(false);
            buchLöschen.setEnabled(false);
            speichern = 1;
        } else {
            Double preis = 0.0;
            String preisI = einBuchPreisFeld.getText();
            try {
                if (!preisI.isEmpty()) {
                    preis = Double.parseDouble(preisI);
                }
                Books.newBook(einBuchLabelFeld.getText(), einBuchISBNFeld.getText(), String.valueOf(preis), einBuchKaufFeld.getText());
            } catch (Exception e) {
                System.out.println(e + " => buchNeuActionPerformed");
                Other.errorWin("der eingegebene Preis enthällt unbekannte Zeichen");
                logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
            }

            buchNeu.setText("Neues Buch");
            buchBearbeiten.setEnabled(true);
            buchLöschen.setEnabled(true);
            speichern = 0;
        }
    }//GEN-LAST:event_buchNeuActionPerformed

    private void buchBearbeitenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buchBearbeitenActionPerformed
        if (speichern == 0) {
            buchBearbeiten.setText("Speichern");
            buchNeu.setEnabled(false);
            buchLöschen.setEnabled(false);
            speichern = 1;
        } else {

            ArrayList<String> book = Books.singleBook(einBuchISBNFeld.getText(), 0);
            if (book.isEmpty()) {
                book = Books.singleBook(einBuchLabelFeld.getText(), 1);
            }

            Double preis = 0.0;
            String preisI = einBuchPreisFeld.getText();
            try {
                if (!preisI.isEmpty()) {
                    preis = Double.parseDouble(preisI);
                }
                Books.editBook(book.get(4), einBuchLabelFeld.getText(), einBuchISBNFeld.getText(), String.valueOf(preis), einBuchKaufFeld.getText());
            } catch (Exception e) {
                System.out.println(e + " => buchBearbeitenActionPerformed");
                logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
                Other.errorWin("der eingegebene Preis enthällt unbekannte Zeichen");
            }

            buchBearbeiten.setText("Buch bearbeiten");
            buchNeu.setEnabled(true);
            buchLöschen.setEnabled(true);
            speichern = 0;

        }
    }//GEN-LAST:event_buchBearbeitenActionPerformed

    private void schuelerKlassenBearbeitenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schuelerKlassenBearbeitenActionPerformed
        if (studentClassEdit == 0) {
            schuelerKlassenListNeu.setEnabled(true);
            schuelerKlassenBearbeiten.setText("beende bearbeiten");
            studentClassEdit = 1;
        } else {
            schuelerKlassenListNeu.setEnabled(false);
            schuelerKlassenBearbeiten.setText("Bearbeiten");
            studentClassEdit = 0;
        }
    }//GEN-LAST:event_schuelerKlassenBearbeitenActionPerformed

    private void ausgebenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ausgebenActionPerformed
        Copies.distributeCopy(ausgebenIDFeld.getText(), schuelerId, ausgebenKaufenFeld.getText());
        ausgebenIDFeld.setText("");
    }//GEN-LAST:event_ausgebenActionPerformed

    private void buecherSchuelerTblAktActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buecherSchuelerTblAktActionPerformed
        ArrayList<String> names = Students.BookList(schuelerId);

        while (schuelerBuecherModel.getRowCount() != 0) {
            schuelerBuecherModel.removeRow(0);
        }

        for (int i = 0; i <= names.size() - 5; i = i + 5) {
            Object[] obj = {i / 5 + 1, names.get(i), names.get(i + 1), Other.dateToNormal(names.get(i + 2)), names.get(i + 3), names.get(i + 4)};
            schuelerBuecherModel.addRow(obj);
        }
        schuelerBuecherTbl.setModel(schuelerBuecherModel);
    }//GEN-LAST:event_buecherSchuelerTblAktActionPerformed

    private void buecherTblAktActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buecherTblAktActionPerformed
        currentPanel = 3;
        UpdateTable(Books.BookList());
    }//GEN-LAST:event_buecherTblAktActionPerformed

    private void neuKlasseBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_neuKlasseBtnMouseClicked
        String klasseNew = neuKlasseFeld.getText();
        if (klasseNew.isEmpty()) {

        } else {
            Classes.newClass(klasseNew);
        }
    }//GEN-LAST:event_neuKlasseBtnMouseClicked

    private void schuelerExportPreislisteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_schuelerExportPreislisteMouseClicked
        basePanel.setSelectedIndex(9);

        schuelerRadioButton.setSelected(true);
        schuelerRadioButtonActionPerformed(null);

        superSelectComboBox.setSelectedItem(schuelerKlassenList.getModel().getElementAt(0));
        superSelectComboBoxActionPerformed(null);

        pdfExportAuswahlSelectListModel.addElement(schuelerName.getText());
        pdfExportAuswahlSelectList.setModel(pdfExportAuswahlSelectListModel);

        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[0]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[1]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[3]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[6]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[7]);
        pdfExportOpSelectList2.setModel(pdfExportOpSelectModel);

        sumPrice.setEnabled(true);
        sumPrice.setSelected(true);

        numCheckBox.setSelected(false);
    }//GEN-LAST:event_schuelerExportPreislisteMouseClicked

    private void klasseExportPreislistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_klasseExportPreislistActionPerformed
        basePanel.setSelectedIndex(9);

        schuelerRadioButton.setSelected(true);
        schuelerRadioButtonActionPerformed(null);

        superSelectComboBox.setSelectedItem(momentaneKlasse);
        superSelectComboBoxActionPerformed(null);

        pdfExportAddAllesButtonActionPerformed(evt);

        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[0]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[1]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[3]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[6]);
        pdfExportOpSelectModel.addElement(pdfExportSchuelerOpList[7]);
        pdfExportOpSelectList2.setModel(pdfExportOpSelectModel);

        sumPrice.setEnabled(true);
        sumPrice.setSelected(true);

        numCheckBox.setSelected(false);
    }//GEN-LAST:event_klasseExportPreislistActionPerformed

    private void kopieBarcodeErneutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kopieBarcodeErneutActionPerformed
        try {
            PDF_Export.barcodePDF(Integer.parseInt(momentaneKopie), 1);

            eineKopieSuchen.requestFocus();
            eineKopieSuchen.setCaretPosition(0);
            eineKopieSuchen.selectAll();
        } catch (IOException | DocumentException e) {
            System.out.println(e + " => Cant print Barcode of Copie");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
    }//GEN-LAST:event_kopieBarcodeErneutActionPerformed

    private void kopieEinsammelnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_kopieEinsammelnMouseClicked
        Copies.collectCopy(momentaneKopie);
        eineKopieSuchen.requestFocus();
        eineKopieSuchen.setCaretPosition(0);
        eineKopieSuchen.selectAll();

        try {
            Robot Robo = new Robot();
            Robo.keyPress(KeyEvent.VK_ENTER);
            Robo.keyRelease(KeyEvent.VK_ENTER);
        } catch (AWTException aWTException) {
            System.out.println(aWTException + " => Cant use/create Robot");
        }
    }//GEN-LAST:event_kopieEinsammelnMouseClicked

    private void eineKopieSuchenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eineKopieSuchenActionPerformed
        try {
            int testInteger = Integer.parseInt(eineKopieSuchen.getText());
        } catch (Exception e) {
            System.out.println(e + " => Cant convert Input to Integer");
            Other.errorWin("Die Eingabe darf nur aus einer Zahl bestehen");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
            return;
        }

        momentaneKopie = eineKopieSuchen.getText();
        ArrayList<String> kopie = Copies.Singlecopy(momentaneKopie);
        eineKopieSuchen.selectAll();

        kopieLabel.setText(kopie.get(0));
        kopieFore.setText(kopie.get(7));
        kopieSur.setText(kopie.get(8));
        kopieDistributed.setText(Other.dateToNormal(kopie.get(2)));
        kopieBought.setText(kopie.get(4));
        kopiePaid.setText(kopie.get(6));

        try {
            kopieClass.setText((Students.SingelStudentClasses(kopie.get(1))).get(0));
        } catch (Exception e) {
            System.out.println(e + " => Cant show Class of Student of Copy");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
            kopieClass.setText("");
        }

        try {
            PicEinzelneKopie.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sbv/pictures/Buch_"
                    + kopie.get(9)
                    + ".jpg")));
        } catch (Exception e) {
            System.out.println(e + " => Cant show BookPic");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
            PicEinzelneKopie.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sbv/pictures/missingPicture.png")));
        }
    }//GEN-LAST:event_eineKopieSuchenActionPerformed

    private void einsammelnEingabeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_einsammelnEingabeActionPerformed
        String barcode = einsammelnEingabe.getText();
        ArrayList<String> kopie = Copies.Singlecopy(barcode);

        String classe;
        try {
            classe = (Students.SingelStudentClasses(kopie.get(1)).get(0));
        } catch (Exception e) {
            System.out.println(e + " => Cant show Class of Student of Copy");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
            classe = "none";
        }

        String newRow[] = {String.valueOf(einsammelTabelleModel.getRowCount() + 1), //N
            kopie.get(0), //Label
            barcode, //Barcode
            kopie.get(7).concat(" ").concat(kopie.get(8)), //Name
            classe, //Class
            (Other.dateToNormal(kopie.get(2))), //Distributed
            kopie.get(4), //Bought
            kopie.get(6)}; //Paid

        einsammelTabelleModel.addRow(newRow);

        einsammelnTabelle.setModel(einsammelTabelleModel);

        if (einsammelnColSizeSet) {
            einsammelnColSizeSet = false;
            for (int i = 0; i < einsammelnColSize.length; i++) {
                col = einsammelnTabelle.getColumnModel().getColumn(i);
                col.setPreferredWidth(einsammelnColSize[i]);
            }
            einsammelnTabelle.setAutoResizeMode(1);
        }

        try {
            einsammelnPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sbv/pictures/Buch_"
                    + Books.singleBook(kopie.get(0), 1).get(4)
                    + ".jpg")));
        } catch (Exception e) {
            System.out.println(e + " => Cant show BookPic Einsammeln");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
            einsammelnPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sbv/pictures/missingPicture.png")));
        }
        einsammelnPic.setVisible(true);

        einsammelnEingabe.selectAll();
        einsammelnAlles.setEnabled(true);
    }//GEN-LAST:event_einsammelnEingabeActionPerformed

    private void einsammelnEintragLoeschenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_einsammelnEintragLoeschenActionPerformed
        while (einsammelnTabelle.getSelectedRow() != -1) {
            einsammelTabelleModel.removeRow(einsammelnTabelle.getSelectedRow());
        }

        for (int i = 0; i < einsammelTabelleModel.getRowCount(); i++) {
            einsammelTabelleModel.setValueAt(i + 1, i, 0);
        }

        if (einsammelTabelleModel.getRowCount() == 0) {
            einsammelnAlles.setEnabled(false);
        }

        einsammelnEintragLoeschen.setEnabled(false);

        einsammelnEingabe.requestFocus();
        einsammelnEingabe.setCaretPosition(0);
        einsammelnEingabe.selectAll();
    }//GEN-LAST:event_einsammelnEintragLoeschenActionPerformed

    private void einsammelnAllesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_einsammelnAllesActionPerformed
        while (einsammelTabelleModel.getRowCount() != 0) {
            Copies.collectCopy((String) einsammelTabelleModel.getValueAt(0, 2));
            einsammelTabelleModel.removeRow(0);
        }
        einsammelnAlles.setEnabled(false);
        einsammelnPic.setVisible(false);

        einsammelnEingabe.requestFocus();
        einsammelnEingabe.setCaretPosition(0);
        einsammelnEingabe.selectAll();
    }//GEN-LAST:event_einsammelnAllesActionPerformed

    private void kopieLöschenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kopieLöschenActionPerformed
        Copies.deleteCopy(momentaneKopie);
        eineKopieSuchen.requestFocus();
        eineKopieSuchen.setCaretPosition(0);
        eineKopieSuchen.selectAll();
    }//GEN-LAST:event_kopieLöschenActionPerformed

    private void ausgebenIDFeldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ausgebenIDFeldActionPerformed
        ausgebenActionPerformed(evt);
    }//GEN-LAST:event_ausgebenIDFeldActionPerformed

    private void einsammelnTabelleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_einsammelnTabelleMouseClicked
        einsammelnEintragLoeschen.setEnabled(true);

        try {
            einsammelnPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sbv/pictures/Buch_"
                    + Books.singleBook((String) einsammelTabelleModel.getValueAt(einsammelnTabelle.getSelectedRow(), 1), 1).get(4)
                    + ".jpg")));
        } catch (Exception e) {
            System.out.println(e + " => Cant show BookPic Einsammeln");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
            einsammelnPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sbv/pictures/missingPicture.png")));
        }
        einsammelnPic.setVisible(true);

        einsammelnEingabe.requestFocus();
        einsammelnEingabe.setCaretPosition(0);
        einsammelnEingabe.selectAll();
    }//GEN-LAST:event_einsammelnTabelleMouseClicked

    private void schuelerRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schuelerRadioButtonActionPerformed
        pdfExportAuswahlSelectListModel.clear();
        pdfExportAuswahlAllesListModel.clear();
        pdfExportOpSelectModel.clear();
        ArrayList<String> data = Classes.getClassNameList();
        Object[] input = new Object[data.size()];
        for (int i = 0; i < data.size(); i++) {
            input[i] = data.get(i);
        }
        superSelectComboBox.setModel(new DefaultComboBoxModel(input));

        superSelectComboBox.setMaximumRowCount(input.length);

        pdfExportAuswahlSelectList.setModel(pdfExportAuswahlSelectListModel);

        pdfExportOpAllesModel.clear();
        for (int i = 0; i < pdfExportSchuelerOpList.length; i++) {
            pdfExportOpAllesModel.addElement(pdfExportSchuelerOpList[i]);
        }
        pdfExportOpAllesList.setModel(pdfExportOpAllesModel);
        pdfExportOpSelectList2.setModel(pdfExportOpSelectModel);

        groupExport.setEnabled(true);
        soloExport.setEnabled(true);
    }//GEN-LAST:event_schuelerRadioButtonActionPerformed

    private void superSelectComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_superSelectComboBoxActionPerformed
        if (schuelerRadioButton.isSelected()) {
            pdfExportAuswahlAllesListModel.removeAllElements();
            ArrayList<String> data = Classes.classList((String) superSelectComboBox.getSelectedItem());
            for (int i = 0; i < data.size(); i = i + 4) {
                pdfExportAuswahlAllesListModel.addElement(data.get(i).concat(" ").concat(data.get(i + 1)));
            }
            pdfExportAuswahlAllesList.setModel(pdfExportAuswahlAllesListModel);
        }
    }//GEN-LAST:event_superSelectComboBoxActionPerformed


    private void pdfExportAddAllesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfExportAddAllesButtonActionPerformed
        for (int i = 0; i < pdfExportAuswahlAllesListModel.getSize(); i++) {
            pdfExportAuswahlSelectListModel.addElement(pdfExportAuswahlAllesListModel.getElementAt(i));
        }
        checkForDoubles();
    }//GEN-LAST:event_pdfExportAddAllesButtonActionPerformed

    private void pdfExportDelAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfExportDelAllButtonActionPerformed
        pdfExportAuswahlSelectListModel.removeAllElements();
    }//GEN-LAST:event_pdfExportDelAllButtonActionPerformed

    private void pdfExportAddSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfExportAddSelectButtonActionPerformed
        int[] selected = pdfExportAuswahlAllesList.getSelectedIndices();
        int id = 0;
        while (id < selected.length) {
            pdfExportAuswahlSelectListModel.addElement(pdfExportAuswahlAllesListModel.getElementAt(selected[id]));
            id++;
        }
        checkForDoubles();
    }//GEN-LAST:event_pdfExportAddSelectButtonActionPerformed

    private void checkForDoubles() {
        ArrayList doppelt = new ArrayList();
        for (int i = 0; i < pdfExportAuswahlSelectListModel.size(); i++) {
            for (int j = i + 1; j < pdfExportAuswahlSelectListModel.size(); j++) {
                if (pdfExportAuswahlSelectListModel.get(i).equals(pdfExportAuswahlSelectListModel.get(j))) {
                    doppelt.add(j);
                }
            }
        }
        int id = 0;
        for (int i = 0; i < pdfExportAuswahlSelectListModel.size(); i++) {
            if (doppelt.contains(i)) {
                pdfExportAuswahlSelectListModel.remove(id);
            } else {
                id++;
            }
        }

        pdfExportAuswahlSelectList.setModel(pdfExportAuswahlSelectListModel);
    }


    private void pdfExportOpAddAllesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfExportOpAddAllesButtonActionPerformed
        for (int i = 0; i < pdfExportOpAllesModel.getSize(); i++) {
            pdfExportOpSelectModel.addElement(pdfExportOpAllesModel.getElementAt(i));
        }
        checkForDoublesOp();
    }//GEN-LAST:event_pdfExportOpAddAllesButtonActionPerformed

    private void pdfExportOpAddSelectButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfExportOpAddSelectButton2ActionPerformed
        int[] selected = pdfExportOpAllesList.getSelectedIndices();
        int id = 0;
        while (id < selected.length) {
            pdfExportOpSelectModel.addElement(pdfExportOpAllesModel.getElementAt(selected[id]));
            id++;
        }
        checkForDoublesOp();
    }//GEN-LAST:event_pdfExportOpAddSelectButton2ActionPerformed

    private void pdfExportOpDelAllButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfExportOpDelAllButton2ActionPerformed
        pdfExportOpSelectModel.removeAllElements();
        sumPrice.setEnabled(false);
    }//GEN-LAST:event_pdfExportOpDelAllButton2ActionPerformed

    private void pdfExportOpDelSelectButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfExportOpDelSelectButton2ActionPerformed
        while (pdfExportOpSelectList2.getSelectedIndex() != -1) {
            pdfExportOpSelectModel.remove(pdfExportOpSelectList2.getSelectedIndex());
        }
        if (pdfExportOpSelectModel.contains(pdfExportSchuelerOpList[7])) {
            sumPrice.setEnabled(false);
        }
    }//GEN-LAST:event_pdfExportOpDelSelectButton2ActionPerformed

    private void checkForDoublesOp() {
        ArrayList doppelt = new ArrayList();
        for (int i = 0; i < pdfExportOpSelectModel.size(); i++) {
            for (int j = i + 1; j < pdfExportOpSelectModel.size(); j++) {
                if (pdfExportOpSelectModel.get(i).equals(pdfExportOpSelectModel.get(j))) {
                    doppelt.add(j);
                }
            }
        }
        int id = 0;
        for (int i = 0; i < pdfExportOpSelectModel.size(); i++) {
            if (doppelt.contains(i)) {
                pdfExportOpSelectModel.remove(id);
            } else {
                id++;
            }
        }

        if (pdfExportOpSelectModel.contains(pdfExportSchuelerOpList[7])) {
            sumPrice.setEnabled(true);
        }

        pdfExportOpSelectList2.setModel(pdfExportOpSelectModel);
    }


    private void pdfExportDelSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfExportDelSelectButtonActionPerformed
        while (pdfExportAuswahlSelectList.getSelectedIndex() != -1) {
            pdfExportAuswahlSelectListModel.remove(pdfExportAuswahlSelectList.getSelectedIndex());
        }
    }//GEN-LAST:event_pdfExportDelSelectButtonActionPerformed

    private void pdfExportPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfExportPrintActionPerformed
        if (pdfExportAuswahlSelectListModel.isEmpty()
                || pdfExportOpSelectModel.isEmpty()
                || !(schuelerRadioButton.isSelected() || klasseRadioButton.isSelected() || buchRadioButton.isSelected())
                || !(groupExport.isSelected() || soloExport.isSelected())) {
            Other.errorWin("Es muss eine Auswahl getroffen werden");
            return;
        }

        pdfExportProgressBar.setValue(0);
        setProgressBarExportString("0%");
        pdfExportProgressBar.setMaximum(pdfExportAuswahlSelectListModel.size());
        pdfExportPrint.setEnabled(false);

        exp = new Thread(exportRunnable);
        exp.start();
    }//GEN-LAST:event_pdfExportPrintActionPerformed

    private void klasseRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_klasseRadioButtonActionPerformed
        pdfExportAuswahlSelectListModel.clear();
        pdfExportAuswahlAllesListModel.clear();
        pdfExportOpSelectModel.clear();
        sumPrice.setEnabled(false);
        ArrayList<String> data = Classes.getClassNameList();
        for (int i = 0; i < data.size(); i++) {
            pdfExportAuswahlAllesListModel.addElement(data.get(i));
        }
        pdfExportAuswahlAllesList.setModel(pdfExportAuswahlAllesListModel);

        superSelectComboBox.setModel(new DefaultComboBoxModel());

        superSelectComboBox.setMaximumRowCount(0);

        pdfExportAuswahlSelectList.setModel(pdfExportAuswahlSelectListModel);

        pdfExportOpAllesModel.clear();
        for (int i = 0; i < pdfExportClassOpList.length; i++) {
            pdfExportOpAllesModel.addElement(pdfExportClassOpList[i]);
        }
        pdfExportOpAllesList.setModel(pdfExportOpAllesModel);
        pdfExportOpSelectList2.setModel(pdfExportOpSelectModel);

        groupExport.setEnabled(true);
        soloExport.setEnabled(true);
    }//GEN-LAST:event_klasseRadioButtonActionPerformed

    private void buchRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buchRadioButtonActionPerformed
        pdfExportAuswahlSelectListModel.clear();
        pdfExportAuswahlAllesListModel.clear();
        pdfExportOpSelectModel.clear();
        sumPrice.setEnabled(false);

        ArrayList<String> data = Books.BookList();
        for (int i = 0; i < data.size(); i = i + 4) {
            pdfExportAuswahlAllesListModel.addElement(data.get(i));
        }
        pdfExportAuswahlAllesList.setModel(pdfExportAuswahlAllesListModel);

        superSelectComboBox.setModel(new DefaultComboBoxModel());

        superSelectComboBox.setMaximumRowCount(0);

        pdfExportAuswahlSelectList.setModel(pdfExportAuswahlSelectListModel);

        pdfExportOpAllesModel.clear();
        for (int i = 0; i < pdfExportBookOpList.length; i++) {
            pdfExportOpAllesModel.addElement(pdfExportBookOpList[i]);
        }
        pdfExportOpAllesList.setModel(pdfExportOpAllesModel);
        pdfExportOpSelectList2.setModel(pdfExportOpSelectModel);

        groupExport.setEnabled(false);
        soloExport.setEnabled(false);
        groupExport.setSelected(true);
    }//GEN-LAST:event_buchRadioButtonActionPerformed

    private void HoverSingleCopieSucheMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleCopieSucheMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip1.setText("wählt die Kopie mit dem");
        ToolTip2.setText("entsprechenden Barcode");
        ToolTip3.setText("aus und zeigt die");
        ToolTip4.setText("entsprechenden Daten an");
    }//GEN-LAST:event_HoverSingleCopieSucheMouseEntered

    private void HoverSingleCopieSucheMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleCopieSucheMouseExited
        ToolTip1.setText("");
        ToolTip2.setText("");
        ToolTip3.setText("");
        ToolTip4.setText("");
    }//GEN-LAST:event_HoverSingleCopieSucheMouseExited

    private void HoverSingleCopieEinsammelnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleCopieEinsammelnMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip1.setText("sammelt die gewählte");
        ToolTip2.setText("Kopie ein und");
        ToolTip3.setText("aktualisiert die Anzeige");
        ToolTip4.setText("");
    }//GEN-LAST:event_HoverSingleCopieEinsammelnMouseEntered

    private void HoverSingleCopieEinsammelnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleCopieEinsammelnMouseExited
        ToolTip1.setText("");
        ToolTip2.setText("");
        ToolTip3.setText("");
        ToolTip4.setText("");
    }//GEN-LAST:event_HoverSingleCopieEinsammelnMouseExited

    private void HoverSingleCopieLoeschenMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleCopieLoeschenMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip1.setText("LÖSCHT die gewählte");
        ToolTip2.setText("Kopie für immer!");
        ToolTip3.setText("");
        ToolTip4.setText("(eine sehr lange Zeit)");
    }//GEN-LAST:event_HoverSingleCopieLoeschenMouseEntered

    private void HoverSingleCopieLoeschenMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleCopieLoeschenMouseExited
        ToolTip1.setText("");
        ToolTip2.setText("");
        ToolTip3.setText("");
        ToolTip4.setText("");
    }//GEN-LAST:event_HoverSingleCopieLoeschenMouseExited

    private void HoverSingleCopieBarcodeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleCopieBarcodeMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip1.setText("der Barcode der");
        ToolTip2.setText("gewählten Kopie wird");
        ToolTip3.setText("erneut erstellt");
        ToolTip4.setText("(muss manuel gedruckt werden)");
    }//GEN-LAST:event_HoverSingleCopieBarcodeMouseEntered

    private void HoverSingleCopieBarcodeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleCopieBarcodeMouseExited
        ToolTip1.setText("");
        ToolTip2.setText("");
        ToolTip3.setText("");
        ToolTip4.setText("");
    }//GEN-LAST:event_HoverSingleCopieBarcodeMouseExited

    private void HoverHomeLizenzMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverHomeLizenzMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip5.setText("eine Auflistung der jeweiligen Berechtigungen befindet sich in der Bedinungsanleitung");
    }//GEN-LAST:event_HoverHomeLizenzMouseEntered

    private void HoverHomeLizenzMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverHomeLizenzMouseExited
        ToolTip5.setText("");
    }//GEN-LAST:event_HoverHomeLizenzMouseExited

    private void HoverSchuelerExportMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSchuelerExportMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip6.setText("läd Voreinstellungen für einen PDF-Export der gewählten Klasse");
    }//GEN-LAST:event_HoverSchuelerExportMouseEntered

    private void HoverSchuelerExportMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSchuelerExportMouseExited
        ToolTip6.setText("");
    }//GEN-LAST:event_HoverSchuelerExportMouseExited

    private void HoverSchuelerPreisExportMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSchuelerPreisExportMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip6.setText("läd Voreinstellungen für einen PDF-Export von Preislisten der gewählten Klasse");
    }//GEN-LAST:event_HoverSchuelerPreisExportMouseEntered

    private void HoverSchuelerPreisExportMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSchuelerPreisExportMouseExited
        ToolTip6.setText("");
    }//GEN-LAST:event_HoverSchuelerPreisExportMouseExited

    private void HoverSchuelerHinzufuegenMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSchuelerHinzufuegenMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip6.setText("erstellt eine neue Klasse mit dem nebenstehenden Namen");
    }//GEN-LAST:event_HoverSchuelerHinzufuegenMouseEntered

    private void HoverSchuelerHinzufuegenMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSchuelerHinzufuegenMouseExited
        ToolTip6.setText("");
    }//GEN-LAST:event_HoverSchuelerHinzufuegenMouseExited

    private void HoverSingleSchuelerEditMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleSchuelerEditMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip7.setText("startet und beendet die");
        ToolTip8.setText("Bearbeitung der Klassenzu-");
        ToolTip9.setText("gehörigkeit des gewählten Schülers");
    }//GEN-LAST:event_HoverSingleSchuelerEditMouseEntered

    private void HoverSingleSchuelerEditMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleSchuelerEditMouseExited
        ToolTip7.setText("");
        ToolTip8.setText("");
        ToolTip9.setText("");
    }//GEN-LAST:event_HoverSingleSchuelerEditMouseExited

    private void HoverSingleSchuelerAusgebenMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleSchuelerAusgebenMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip7.setText("die Kopie des eingegebenen");
        ToolTip8.setText("Barcodes wird an den");
        ToolTip9.setText("gewählten Schüler ausgegeben");
    }//GEN-LAST:event_HoverSingleSchuelerAusgebenMouseEntered

    private void HoverSingleSchuelerAusgebenMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleSchuelerAusgebenMouseExited
        ToolTip7.setText("");
        ToolTip8.setText("");
        ToolTip9.setText("");
    }//GEN-LAST:event_HoverSingleSchuelerAusgebenMouseExited

    private void HoverSingleBookSucheISBNMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookSucheISBNMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip10.setText("wählt das erste Buch aus,");
        ToolTip11.setText("welches das Eingegebene in");
        ToolTip12.setText("der ISBN enthält");
    }//GEN-LAST:event_HoverSingleBookSucheISBNMouseEntered

    private void HoverSingleBookSucheISBNMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookSucheISBNMouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_HoverSingleBookSucheISBNMouseExited

    private void HoverSingleBookSucheLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookSucheLabelMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip10.setText("wählt das erste Buch aus,");
        ToolTip11.setText("welches das Eingegebene im");
        ToolTip12.setText("Namen enthält");
    }//GEN-LAST:event_HoverSingleBookSucheLabelMouseEntered

    private void HoverSingleBookSucheLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookSucheLabelMouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_HoverSingleBookSucheLabelMouseExited

    private void HoverSingleBookKopienMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookKopienMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip10.setText("erstellt Kopien in entsprechender");
        ToolTip11.setText("Anzahl des gewählten Buch");
        ToolTip12.setText("(Barcode muss manuel gedruckt werden)");
    }//GEN-LAST:event_HoverSingleBookKopienMouseEntered

    private void HoverSingleBookKopienMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookKopienMouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_HoverSingleBookKopienMouseExited

    private void HoverSingleBookNeuMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookNeuMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip10.setText("startet und beendet die Eingabe");
        ToolTip11.setText("zur Erstellung eines neuen Buches");
        ToolTip12.setText("(vorherige Eingaben werden verworfen)");
    }//GEN-LAST:event_HoverSingleBookNeuMouseEntered

    private void HoverSingleBookNeuMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookNeuMouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_HoverSingleBookNeuMouseExited

    private void HoverSingleBookBearbeitenMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookBearbeitenMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip10.setText("startet und beendet das");
        ToolTip11.setText("editieren des gewählten Buches");
        ToolTip12.setText("(nur eine Veränderung pro Vorgang)");
    }//GEN-LAST:event_HoverSingleBookBearbeitenMouseEntered

    private void HoverSingleBookBearbeitenMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookBearbeitenMouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_HoverSingleBookBearbeitenMouseExited

    private void HoverSingleBookLoeschenMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookLoeschenMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip10.setText("LÖSCHT das gewählte Buch");
        ToolTip11.setText("für immer!");
        ToolTip12.setText("(eine sehr lange Zeit)");
    }//GEN-LAST:event_HoverSingleBookLoeschenMouseEntered

    private void HoverSingleBookLoeschenMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookLoeschenMouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_HoverSingleBookLoeschenMouseExited

    private void buchLöschenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buchLöschenActionPerformed
        if (speichern == 0) {
            buchLöschen.setText("Bestätigen");
            buchLöschen.setForeground(Color.red);
            buchNeu.setEnabled(false);
            buchBearbeiten.setEnabled(false);
            buchAb.setVisible(true);
            buchAb.setEnabled(true);
            speichern = 1;
        } else {
            Books.delBook(einBuchLabelFeld.getText());
            buchLöschen.setForeground(Color.black);
            buchLöschen.setText("Buch löschen");
            buchNeu.setEnabled(true);
            buchBearbeiten.setEnabled(true);
            buchAb.setVisible(false);
            buchAb.setEnabled(false);
            speichern = 0;
        }
    }//GEN-LAST:event_buchLöschenActionPerformed

    private void buchAbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buchAbActionPerformed
        if (speichern == 1) {
            buchLöschen.setForeground(Color.black);
            buchLöschen.setText("Buch löschen");
            buchNeu.setEnabled(true);
            buchBearbeiten.setEnabled(true);
            buchAb.setVisible(false);
            buchAb.setEnabled(false);
            speichern = 0;
        }
    }//GEN-LAST:event_buchAbActionPerformed

    private void HoverSingleSchuelerVorMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleSchuelerVorMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip7.setText("wählt den nächsten Schüler");
        ToolTip8.setText("der unter \"Schüler\" gewählten");
        ToolTip9.setText("Klasse aus");
    }//GEN-LAST:event_HoverSingleSchuelerVorMouseEntered

    private void HoverSingleSchuelerVorMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleSchuelerVorMouseExited
        ToolTip7.setText("");
        ToolTip8.setText("");
        ToolTip9.setText("");
    }//GEN-LAST:event_HoverSingleSchuelerVorMouseExited

    private void HoverSingleSchuelerZurueckMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleSchuelerZurueckMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip7.setText("wählt den vorherigen Schüler");
        ToolTip8.setText("der unter \"Schüler\" gewählten");
        ToolTip9.setText("Klasse aus");
    }//GEN-LAST:event_HoverSingleSchuelerZurueckMouseEntered

    private void HoverSingleSchuelerZurueckMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleSchuelerZurueckMouseExited
        ToolTip7.setText("");
        ToolTip8.setText("");
        ToolTip9.setText("");
    }//GEN-LAST:event_HoverSingleSchuelerZurueckMouseExited

    private void HoverSingleBookISBNMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookISBNMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip10.setText("die ISBN wird ohne Sonder-");
        ToolTip11.setText("zeichen verwendet");
        ToolTip12.setText("(auch keine Leerzeichen)");
    }//GEN-LAST:event_HoverSingleBookISBNMouseEntered

    private void HoverSingleBookISBNMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookISBNMouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_HoverSingleBookISBNMouseExited

    private void HoverSingleBookKaufMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookKaufMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip10.setText("zeigt an, ob das Buch");
        ToolTip11.setText("gekauft werden muss");
        ToolTip12.setText("(1 = Kaufbuch | 0 = Leihbuch)");
    }//GEN-LAST:event_HoverSingleBookKaufMouseEntered

    private void HoverSingleBookKaufMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookKaufMouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_HoverSingleBookKaufMouseExited

    private void HoverSingleBookPreisMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookPreisMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip10.setText("gibt den Preis des");
        ToolTip11.setText("Buches in Euro an");
        ToolTip12.setText("(der Punkt entspricht dem Komma)");
    }//GEN-LAST:event_HoverSingleBookPreisMouseEntered

    private void HoverSingleBookPreisMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleBookPreisMouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_HoverSingleBookPreisMouseExited

    private void HoverCollectCodeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverCollectCodeMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip13.setText("nach der Bestätigung der Eingabe");
        ToolTip14.setText("wird das Element der Tabelle hinzugefügt");
    }//GEN-LAST:event_HoverCollectCodeMouseEntered

    private void HoverCollectCodeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverCollectCodeMouseExited
        ToolTip13.setText("");
        ToolTip14.setText("");
    }//GEN-LAST:event_HoverCollectCodeMouseExited

    private void HoverCollectLoeschenMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverCollectLoeschenMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip13.setText("entfernt alle selektierten Elemente");
        ToolTip14.setText("aus der Tabelle");
    }//GEN-LAST:event_HoverCollectLoeschenMouseEntered

    private void HoverCollectLoeschenMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverCollectLoeschenMouseExited
        ToolTip13.setText("");
        ToolTip14.setText("");
    }//GEN-LAST:event_HoverCollectLoeschenMouseExited

    private void HoverCollectEinsammelnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverCollectEinsammelnMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip13.setText("sammelt alle Element in der Tabelle");
        ToolTip14.setText("ein und leert die Tabelle");
    }//GEN-LAST:event_HoverCollectEinsammelnMouseEntered

    private void HoverCollectEinsammelnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverCollectEinsammelnMouseExited
        ToolTip13.setText("");
        ToolTip14.setText("");
    }//GEN-LAST:event_HoverCollectEinsammelnMouseExited

    private void HoverSingleSchuelerBezahltMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleSchuelerBezahltMouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        ToolTip7.setText("gibt an, ob das auszugebene");
        ToolTip8.setText("Buch bereits bezahlt ist (1)");
        ToolTip9.setText("oder nicht (0)");
    }//GEN-LAST:event_HoverSingleSchuelerBezahltMouseEntered

    private void HoverSingleSchuelerBezahltMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_HoverSingleSchuelerBezahltMouseExited
        ToolTip7.setText("");
        ToolTip8.setText("");
        ToolTip9.setText("");
    }//GEN-LAST:event_HoverSingleSchuelerBezahltMouseExited

    private void schuelerKlassenListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_schuelerKlassenListMouseClicked
        if (studentClassEdit == 1) {
            int index = schuelerKlassenList.locationToIndex(evt.getPoint());
            Students.removeFromClass(schuelerId, schuelerKlassenList.getModel().getElementAt(index).toString());
        }
        schuelerKlassenList.setListData(Students.SingelStudentClasses(schuelerId).toArray());
    }//GEN-LAST:event_schuelerKlassenListMouseClicked

    private PdfPTable schuelerEx(String studentID) {
        ArrayList<String> source = Students.BookList(studentID); //label, buy, distributed, paid, sbm_copies.ID
        ArrayList<String> prices = Copies.copyBill(studentID);

        int width = pdfExportOpSelectModel.size();
        int heigth = source.size() / 5;

        pay = 0;
        int sumPos = 1;

        PdfPTable table;
        float[] columnWidth;
        int start;
        if (numCheckBox.isSelected()) {
            columnWidth = new float[width + 1];
            columnWidth[0] = 2;
            start = 1;
        } else {
            columnWidth = new float[width];
            start = 0;
        }

        for (int i = start; i < columnWidth.length; i++) {
            Object item = pdfExportOpSelectModel.getElementAt(i - start);

            if (item.equals(pdfExportSchuelerOpList[0])) {  //label
                columnWidth[i] = 9;
                continue;
            }
            if (item.equals(pdfExportSchuelerOpList[1])) {  //buy
                columnWidth[i] = 2;
                continue;
            }
            if (item.equals(pdfExportSchuelerOpList[2])) {  //distributed
                columnWidth[i] = 5;
                continue;
            }
            if (item.equals(pdfExportSchuelerOpList[3])) {  //paid
                columnWidth[i] = 2;
                continue;
            }
            if (item.equals(pdfExportSchuelerOpList[4])) {  //sbm_copies.ID
                columnWidth[i] = 3;
                continue;
            }
            if (item.equals(pdfExportSchuelerOpList[5])) {  //ISBN
                columnWidth[i] = 5;
                continue;
            }
            if (item.equals(pdfExportSchuelerOpList[6])) {  //Preis
                columnWidth[i] = 3;
                continue;
            }
            if (item.equals(pdfExportSchuelerOpList[7])) {  //zuzahlen
                columnWidth[i] = 3;
                continue;
            }

            Other.errorWin("Fatal Error");

        }

        table = new PdfPTable(columnWidth);

        table.setSpacingBefore(25);
        table.setSpacingAfter(25);

        //Überschriften
        if (numCheckBox.isSelected()) {
            table.addCell(new PdfPCell(new Phrase("N", format)));
        }
        for (int i = 0; i < width; i++) {
            table.addCell(new PdfPCell(new Phrase((String) pdfExportOpSelectModel.get(i), format)));
        }

        //Inhalt
        for (int i = 0; i < heigth; i++) {
            if (numCheckBox.isSelected()) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), format)));
            }
            for (int j = 0; j < width; j++) {
                Object item = pdfExportOpSelectModel.getElementAt(j);
                if (item.equals(pdfExportSchuelerOpList[0])) {  //label
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * 5 + 0), format)));
                    continue;
                }
                if (item.equals(pdfExportSchuelerOpList[1])) {  //buy
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * 5 + 1), format)));
                    continue;
                }
                if (item.equals(pdfExportSchuelerOpList[2])) {  //distributed
                    table.addCell(new PdfPCell(new Phrase(Other.dateToNormal((String) source.get(i * 5 + 2)), format)));
                    continue;
                }
                if (item.equals(pdfExportSchuelerOpList[3])) {  //paid
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * 5 + 3), format)));
                    continue;
                }
                if (item.equals(pdfExportSchuelerOpList[4])) {  //code
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * 5 + 4), format)));
                    continue;
                }
                if (item.equals(pdfExportSchuelerOpList[5])) {  //ISBN
                    table.addCell(new PdfPCell(new Phrase(Books.singleBook(
                            source.get(i * 5 + 0), 1)
                            .get(1), format)));
                    continue;
                }
                if (item.equals(pdfExportSchuelerOpList[6])) {  //Preis
                    table.addCell(new PdfPCell(new Phrase((String) prices.get(i), format)));
                    continue;
                }
                if (item.equals(pdfExportSchuelerOpList[7])) {  //zuzahlen
                    sumPos = j;
                    if (source.get(i * 5 + 1).equals("1") && source.get(i * 5 + 3).equals("0")) {
                        table.addCell(new PdfPCell(new Phrase(prices.get(i), format)));
                        pay = pay + Double.parseDouble(prices.get(i));
                        continue;
                    }
                    table.addCell(new PdfPCell(new Phrase("", format)));
                    continue;
                }

                Other.errorWin("Fatal Error");
            }
        }

        return table;
    }

    private PdfPTable classEx(String classID) {
        ArrayList<String> source = Classes.classList(classID); //forename, surname, birth, student_ID

        int width = pdfExportOpSelectModel.size();
        int heigth = source.size() / 4;

        PdfPTable table;
        float[] columnWidth;
        int start;
        if (numCheckBox.isSelected()) {
            columnWidth = new float[width + 1];
            columnWidth[0] = 2;
            start = 1;
        } else {
            columnWidth = new float[width];
            start = 0;
        }

        for (int i = start; i < columnWidth.length; i++) {
            Object item = pdfExportOpSelectModel.getElementAt(i - start);

            if (item.equals(pdfExportClassOpList[0])) {  //forename
                columnWidth[i] = 7;
                continue;
            }
            if (item.equals(pdfExportClassOpList[1])) {  //surname
                columnWidth[i] = 7;
                continue;
            }
            if (item.equals(pdfExportClassOpList[2])) {  //birth
                columnWidth[i] = 7;
                continue;
            }
            if (item.equals(pdfExportClassOpList[3])) {  //student_ID
                columnWidth[i] = 3;
                continue;
            }
            if (item.equals(pdfExportClassOpList[4])) {  //all
                columnWidth[i] = 2;
                continue;
            }
            if (item.equals(pdfExportClassOpList[5])) {  //ret
                columnWidth[i] = 2;
                continue;
            }

            Other.errorWin("Fatal Error");

        }

        table = new PdfPTable(columnWidth);

        table.setSpacingBefore(25);
        table.setSpacingAfter(25);

        //Überschriften
        if (numCheckBox.isSelected()) {
            table.addCell(new PdfPCell(new Phrase("N", format)));
        }
        for (int i = 0; i < width; i++) {
            table.addCell(new PdfPCell(new Phrase((String) pdfExportOpSelectModel.get(i), format)));
        }

        //Inhalt
        for (int i = 0; i < heigth; i++) {
            if (numCheckBox.isSelected()) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), format)));
            }
            for (int j = 0; j < width; j++) {
                Object item = pdfExportOpSelectModel.getElementAt(j);
                if (item.equals(pdfExportClassOpList[0])) {  //forename
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * 4 + 0), format)));
                    continue;
                }
                if (item.equals(pdfExportClassOpList[1])) {  //surname
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * 4 + 1), format)));
                    continue;
                }
                if (item.equals(pdfExportClassOpList[2])) {  //birth
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * 4 + 2), format)));
                    continue;
                }
                if (item.equals(pdfExportClassOpList[3])) {  //student_ID
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * 4 + 3), format)));
                    continue;
                }
                if (item.equals(pdfExportClassOpList[4])) {  //all
                    table.addCell(new PdfPCell(new Phrase(Students.copiesCount(source.get(i * 4 + 3)), format)));
                    continue;
                }
                if (item.equals(pdfExportClassOpList[5])) {  //ret
                    table.addCell(new PdfPCell(new Phrase(Students.CopiesToReturn(source.get(i * 4 + 3)), format)));
                    continue;
                }

                Other.errorWin("Fatal Error");
            }
        }

        return table;
    }

    private PdfPTable bookEx() {
        ArrayList<String> source = new ArrayList();
        for (int i = 0; i < pdfExportAuswahlSelectListModel.size(); i++) {
            ArrayList<String> source2 = Books.singleBook((String) pdfExportAuswahlSelectListModel.get(i), 1);   //label, isbn, price, buy, ID
            for (int j = 0; j < source2.size(); j++) {
                source.add(source2.get(j));
            }
        }

        int width = pdfExportOpSelectModel.size();
        int heigth = source.size() / 5;

        pdfExportProgressBar.setMaximum(heigth);

        PdfPTable table;
        float[] columnWidth;
        int start;
        if (numCheckBox.isSelected()) {
            columnWidth = new float[width + 1];
            columnWidth[0] = 2;
            start = 1;
        } else {
            columnWidth = new float[width];
            start = 0;
        }

        for (int i = start; i < columnWidth.length; i++) {
            Object item = pdfExportOpSelectModel.getElementAt(i - start);

            if (item.equals(pdfExportBookOpList[0])) {  //label
                columnWidth[i] = 12;
                continue;
            }
            if (item.equals(pdfExportBookOpList[1])) {  //isbn
                columnWidth[i] = 5;
                continue;
            }
            if (item.equals(pdfExportBookOpList[2])) {  //price
                columnWidth[i] = 4;
                continue;
            }
            if (item.equals(pdfExportBookOpList[3])) {  //buy
                columnWidth[i] = 2;
                continue;
            }
            if (item.equals(pdfExportBookOpList[4])) {  //ID
                columnWidth[i] = 2;
                continue;
            }
            if (item.equals(pdfExportBookOpList[5])) {  //ges
                columnWidth[i] = 3;
                continue;
            }
            if (item.equals(pdfExportBookOpList[6])) {  //aus
                columnWidth[i] = 3;
                continue;
            }
            if (item.equals(pdfExportBookOpList[7])) {  //lager
                columnWidth[i] = 3;
                continue;
            }

            Other.errorWin("Fatal Error");

        }

        table = new PdfPTable(columnWidth);

        table.setSpacingBefore(25);
        table.setSpacingAfter(25);

        //Überschriften
        if (numCheckBox.isSelected()) {
            table.addCell(new PdfPCell(new Phrase("N", format)));
        }
        for (int i = 0; i < width; i++) {
            table.addCell(new PdfPCell(new Phrase((String) pdfExportOpSelectModel.get(i), format)));
        }

        //Inhalt
        for (int i = 0; i < heigth; i++) {
            if (numCheckBox.isSelected()) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), format)));
            }

            String bookId = source.get(i * 5 + 4);

            pdfExportProgressBar.setValue(i);
            setProgressBarExportString(source.get(i * 5 + 0));

            for (int j = 0; j < width; j++) {
                Object item = pdfExportOpSelectModel.getElementAt(j);

                if (item.equals(pdfExportBookOpList[0])) {  //label
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * 5 + 0), format)));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[1])) {  //isbn
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * 5 + 1), format)));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[2])) {  //price
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * 5 + 2), format)));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[3])) {  //buy
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * 5 + 3), format)));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[4])) {  //ID
                    table.addCell(new PdfPCell(new Phrase(bookId, format)));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[5])) {  //ges
                    table.addCell(new PdfPCell(new Phrase(Copies.SingleCopyCountTotal(bookId), format)));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[6])) {  //aus
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(Integer.parseInt(Copies.SingleCopyCountTotal(bookId)) - Integer.parseInt(Copies.copiesInStock(bookId))), format)));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[7])) {  //lager
                    table.addCell(new PdfPCell(new Phrase(Copies.copiesInStock(bookId), format)));
                    continue;
                }

                Other.errorWin("Fatal Error");
            }
        }

        return table;
    }

    private void setProgressBarExportString(String text) {
        if (text.length() > 22) {
            pdfExportProgressBar.setString(text.substring(0, 22));
        } else {
            pdfExportProgressBar.setString(text);
        }
    }

    /**
     * @param args the command line arguments
     * @param user
     * @param lizenz
     */
    public static void main(String args[], String user, String lizenz) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException e) {
            System.out.println(e + " => main");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }
        //</editor-fold>

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Oberflaeche().setVisible(true);
            }
        });
        Oberflaeche.user = user;
        Oberflaeche.lizenz = Integer.parseInt(lizenz);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel HoverCollectCode;
    private javax.swing.JLabel HoverCollectEinsammeln;
    private javax.swing.JLabel HoverCollectLoeschen;
    private javax.swing.JLabel HoverHomeLizenz;
    private javax.swing.JLabel HoverSchuelerExport;
    private javax.swing.JLabel HoverSchuelerHinzufuegen;
    private javax.swing.JLabel HoverSchuelerPreisExport;
    private javax.swing.JLabel HoverSingleBookBearbeiten;
    private javax.swing.JLabel HoverSingleBookISBN;
    private javax.swing.JLabel HoverSingleBookKauf;
    private javax.swing.JLabel HoverSingleBookKopien;
    private javax.swing.JLabel HoverSingleBookLoeschen;
    private javax.swing.JLabel HoverSingleBookNeu;
    private javax.swing.JLabel HoverSingleBookPreis;
    private javax.swing.JLabel HoverSingleBookSucheISBN;
    private javax.swing.JLabel HoverSingleBookSucheLabel;
    private javax.swing.JLabel HoverSingleCopieBarcode;
    private javax.swing.JLabel HoverSingleCopieEinsammeln;
    private javax.swing.JLabel HoverSingleCopieLoeschen;
    private javax.swing.JLabel HoverSingleCopieSuche;
    private javax.swing.JLabel HoverSingleSchuelerAusgeben;
    private javax.swing.JLabel HoverSingleSchuelerBezahlt;
    private javax.swing.JLabel HoverSingleSchuelerEdit;
    private javax.swing.JLabel HoverSingleSchuelerVor;
    private javax.swing.JLabel HoverSingleSchuelerZurueck;
    private javax.swing.JLabel PicEinzelneKopie;
    private javax.swing.JLabel ToolTip1;
    private javax.swing.JLabel ToolTip10;
    private javax.swing.JLabel ToolTip11;
    private javax.swing.JLabel ToolTip12;
    private javax.swing.JLabel ToolTip13;
    private javax.swing.JLabel ToolTip14;
    private javax.swing.JLabel ToolTip2;
    private javax.swing.JLabel ToolTip3;
    private javax.swing.JLabel ToolTip4;
    private javax.swing.JLabel ToolTip5;
    private javax.swing.JLabel ToolTip6;
    private javax.swing.JLabel ToolTip7;
    private javax.swing.JLabel ToolTip8;
    private javax.swing.JLabel ToolTip9;
    private javax.swing.JButton ausgeben;
    private javax.swing.JTextField ausgebenIDFeld;
    private javax.swing.JTextField ausgebenKaufenFeld;
    public javax.swing.JTabbedPane basePanel;
    private javax.swing.JButton buchAb;
    private javax.swing.JButton buchBearbeiten;
    private javax.swing.JButton buchLöschen;
    private javax.swing.JButton buchNeu;
    private javax.swing.JRadioButton buchRadioButton;
    private javax.swing.JButton buecherSchuelerTblAkt;
    private javax.swing.JPanel buecherTab;
    private javax.swing.JTable buecherTbl;
    private javax.swing.JButton buecherTblAkt;
    private javax.swing.JTextField einBuchISBNFeld;
    private javax.swing.JLabel einBuchISBNL;
    private javax.swing.JTextField einBuchKaufFeld;
    private javax.swing.JTextField einBuchLabelFeld;
    private javax.swing.JTextField einBuchPreisFeld;
    private javax.swing.JPanel einBuchTab;
    private javax.swing.JPanel einKopieTab;
    private javax.swing.JPanel einSchuelerTab;
    private javax.swing.JTextField eineKopieSuchen;
    private javax.swing.JButton einsammelnAlles;
    private javax.swing.JTextField einsammelnEingabe;
    private javax.swing.JButton einsammelnEintragLoeschen;
    private javax.swing.JLabel einsammelnPic;
    private javax.swing.JPanel einsammelnTab;
    private javax.swing.JTable einsammelnTabelle;
    private javax.swing.ButtonGroup exportFilesGroup;
    private javax.swing.JPanel exportTab;
    private javax.swing.JLabel freieBuecher;
    private javax.swing.JRadioButton groupExport;
    private javax.swing.JPanel homeTab;
    private javax.swing.JTextField isbnSuche;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JButton klasseExportBtn;
    private javax.swing.JButton klasseExportPreislist;
    private javax.swing.JRadioButton klasseRadioButton;
    public static javax.swing.JList klassenList;
    private javax.swing.JButton kopieBarcodeErneut;
    private javax.swing.JLabel kopieBought;
    private javax.swing.JLabel kopieClass;
    private javax.swing.JLabel kopieDistributed;
    private javax.swing.JButton kopieEinsammeln;
    private javax.swing.JLabel kopieFore;
    private javax.swing.JLabel kopieLabel;
    private javax.swing.JButton kopieLöschen;
    private javax.swing.JLabel kopiePaid;
    private javax.swing.JLabel kopieSur;
    private javax.swing.JTextField labelSuche;
    private javax.swing.JLabel lizenzName;
    private javax.swing.JButton neuKlasseBtn;
    private javax.swing.JTextField neuKlasseFeld;
    private javax.swing.JTextField neuKopieAnzahl;
    private javax.swing.JButton neuKopieBtn;
    private javax.swing.JCheckBox numCheckBox;
    private javax.swing.JButton pdfExportAddAllesButton;
    private javax.swing.JButton pdfExportAddSelectButton;
    private javax.swing.JList pdfExportAuswahlAllesList;
    private javax.swing.JList pdfExportAuswahlSelectList;
    private javax.swing.ButtonGroup pdfExportButtonGroup;
    private javax.swing.JButton pdfExportDelAllButton;
    private javax.swing.JButton pdfExportDelSelectButton;
    private javax.swing.JButton pdfExportOpAddAllesButton;
    private javax.swing.JButton pdfExportOpAddSelectButton2;
    private javax.swing.JList pdfExportOpAllesList;
    private javax.swing.JButton pdfExportOpDelAllButton2;
    private javax.swing.JButton pdfExportOpDelSelectButton2;
    private javax.swing.JList pdfExportOpSelectList2;
    private javax.swing.JButton pdfExportPrint;
    private javax.swing.JProgressBar pdfExportProgressBar;
    private javax.swing.JTable schuelerBuecherTbl;
    private javax.swing.JLabel schuelerCount;
    private javax.swing.JButton schuelerExport;
    private javax.swing.JButton schuelerExportPreisliste;
    private javax.swing.JLabel schuelerGeburt;
    private javax.swing.JLabel schuelerID;
    private javax.swing.JButton schuelerKlassenBearbeiten;
    private javax.swing.JList schuelerKlassenList;
    private javax.swing.JList schuelerKlassenListNeu;
    private javax.swing.JLabel schuelerName;
    private javax.swing.JRadioButton schuelerRadioButton;
    public javax.swing.JPanel schuelerTab;
    private javax.swing.JTable schuelerTbl;
    private javax.swing.JPanel schuelerTblPanel;
    private javax.swing.JButton schuelerWeiter;
    private javax.swing.JButton schuelerZurueck;
    private javax.swing.JLabel schuelerZurueckAnzahl;
    private javax.swing.JRadioButton soloExport;
    private javax.swing.JCheckBox sumPrice;
    private javax.swing.JComboBox superSelectComboBox;
    private javax.swing.JLabel welcome;
    // End of variables declaration//GEN-END:variables
}
