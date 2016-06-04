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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFileChooser;

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
    static private boolean buecherFColSizeSet = true;

    private static final String buecherKlasseCol[] = {"Label", "ISBN"};
    DefaultTableModel buecherKlasseModel = new DefaultTableModel(buecherKlasseCol, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    static private final int buecherKlasseColSize[] = {409, 80};
    static private boolean buecherKlasseColSizeSet = true;

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
    static private int skBearbeiten = 0;
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

                    File savefile = chooser.getSelectedFile();
                    String pathName2 = savefile.getPath();
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

                    File savefile = chooser.getSelectedFile();
                    final String pathName2 = savefile.getPath();

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

        if (currentPanel == 4) {
            while (buecherKlasseModel.getRowCount() != 0) {
                buecherKlasseModel.removeRow(0);
            }

            for (int c = 0; c <= data.size() - 2; c = c + 2) {
                Object[] obj = {data.get(c), data.get(c + 1)};
                buecherKlasseModel.addRow(obj);
            }
            buecherKlassenTbl.setModel(buecherKlasseModel);

            if (buecherKlasseColSizeSet) {
                buecherKlasseColSizeSet = false;
                for (int i = 0; i < buecherKlasseColSize.length; i++) {
                    col = buecherKlassenTbl.getColumnModel().getColumn(i);
                    col.setPreferredWidth(buecherKlasseColSize[i]);
                }
                buecherKlassenTbl.setAutoResizeMode(1);
            }
        }

        if (currentPanel == 5) {
            while (buecherKlasseModel.getRowCount() != 0) {
                buecherKlasseModel.removeRow(0);
            }

            for (int d = 0; d <= data.size() - 2; d = d + 2) {
                Object[] obj = {data.get(d), data.get(d + 1)};
                buecherKlasseModel.addRow(obj);
            }
            buecherInKlasseTbl.setModel(buecherKlasseModel);

            if (buecherKlasseColSizeSet) {
                buecherKlasseColSizeSet = false;
                for (int i = 0; i < buecherKlasseColSize.length; i++) {
                    col = buecherKlassenTbl.getColumnModel().getColumn(i);
                    col.setPreferredWidth(buecherKlasseColSize[i]);
                }
                buecherKlassenTbl.setAutoResizeMode(1);
            }
        }

        if (currentPanel == 6) {
            while (buecherModel.getRowCount() != 0) {
                buecherModel.removeRow(0);
            }

            for (int d = 0; d <= data.size() - 4; d = d + 4) {
                Object[] obj = {data.get(d), data.get(d + 1), data.get(d + 2), data.get(d + 3)};
                buecherModel.addRow(obj);
            }
            buecherFKlassenTbl.setModel(buecherModel);

            if (buecherFColSizeSet) {
                buecherFColSizeSet = false;
                for (int i = 0; i < buecherColSize.length; i++) {
                    col = buecherFKlassenTbl.getColumnModel().getColumn(i);
                    col.setPreferredWidth(buecherColSize[i]);
                }
                buecherFKlassenTbl.setAutoResizeMode(1);
            }
        }
    }

    public Oberflaeche() {
        initComponents();

        //this.setExtendedState(Frame.MAXIMIZED_BOTH);
        schuelerCount.setText(Home.StudentsCount());
        freieBuecher.setText(Home.CauchtCopyCount());

        switch (lizenz) {
            case 3:
                neuKlasseBtn.setEnabled(false);                 //neue Klasse anlegen
                neuKlasseBtn.setVisible(false);
                neuKlasseFeld.setVisible(false);
                jScrollPane5.setVisible(false);                 //Klassenzugehörigkeit eines Schülers bearbeiten
                jScrollPane8.setVisible(false);
                schuelerKlassenBearbeiten.setEnabled(false);
                schuelerKlassenBearbeiten.setVisible(false);
                neuKopieBtn.setEnabled(false);                  //neue Kopien eines Buches erstellen
                neuKopieBtn.setVisible(false);
                jLabel18.setVisible(false);
                neuKopieAnzahl.setVisible(false);
                buchNeu.setEnabled(false);                      //neue Bücher erstellen
                buchNeu.setVisible(false);
                kopieBarcodeErneut.setEnabled(false);           //Barcode erneut ausdrucken
                kopieBarcodeErneut.setVisible(false);

            case 2:
                buchLöschen.setEnabled(false);                  //Bücher löschen
                buchLöschen.setVisible(false);
                buchBearbeiten.setEnabled(false);               //Bücher bearbeiten
                buchBearbeiten.setVisible(false);
                kopieLöschen.setEnabled(false);                 //Kopien löschen
                kopieLöschen.setVisible(false);

            case 1:

                break;

            default:

        }

        lizenzName.setText(lizenzenNamen[lizenz]);

        welcome.setText("Willkommen " + user);
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
        Hover5 = new javax.swing.JLabel();
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
        Hover6 = new javax.swing.JLabel();
        Hover7 = new javax.swing.JLabel();
        Hover8 = new javax.swing.JLabel();
        Hover9 = new javax.swing.JLabel();
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
        Hover11 = new javax.swing.JLabel();
        Hover12 = new javax.swing.JLabel();
        Hover10 = new javax.swing.JLabel();
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
        Hover13 = new javax.swing.JLabel();
        Hover14 = new javax.swing.JLabel();
        Hover15 = new javax.swing.JLabel();
        Hover16 = new javax.swing.JLabel();
        Hover17 = new javax.swing.JLabel();
        Hover18 = new javax.swing.JLabel();
        ToolTip10 = new javax.swing.JLabel();
        ToolTip11 = new javax.swing.JLabel();
        ToolTip12 = new javax.swing.JLabel();
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
        Hover1 = new javax.swing.JLabel();
        Hover2 = new javax.swing.JLabel();
        Hover3 = new javax.swing.JLabel();
        Hover4 = new javax.swing.JLabel();
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
        klassenTab = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        buchKlassenList = new javax.swing.JList();
        jScrollPane7 = new javax.swing.JScrollPane();
        buecherKlassenTbl = new javax.swing.JTable();
        klassenBearbeitenTab = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        buchKlassenList1 = new javax.swing.JList();
        jScrollPane10 = new javax.swing.JScrollPane();
        buecherInKlasseTbl = new javax.swing.JTable();
        jScrollPane11 = new javax.swing.JScrollPane();
        buecherFKlassenTbl = new javax.swing.JTable();
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

        Hover5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover5.setText("?");
        Hover5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover5MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover5MouseExited(evt);
            }
        });

        ToolTip5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ToolTip5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

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
                        .addComponent(Hover5, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ToolTip5, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 268, Short.MAX_VALUE)
                .addComponent(lizenzName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(homeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ToolTip5, javax.swing.GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
                    .addComponent(Hover5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        Hover6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover6.setText("?");
        Hover6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover6MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover6MouseExited(evt);
            }
        });

        Hover7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover7.setText("?");
        Hover7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover7MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover7MouseExited(evt);
            }
        });

        Hover8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover8.setText("?");
        Hover8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover8MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover8MouseExited(evt);
            }
        });

        Hover9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover9.setText("?");
        Hover9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover9MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover9MouseExited(evt);
            }
        });

        ToolTip6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

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
                                .addComponent(Hover9, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(299, 299, 299)
                                .addComponent(Hover6, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(140, 140, 140)
                                .addComponent(Hover7, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(klasseExportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(Hover8, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                        .addComponent(Hover9))
                    .addComponent(klasseExportBtn)
                    .addGroup(schuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(klasseExportPreislist)
                        .addComponent(Hover7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Hover8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(Hover6))
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

        ToolTip7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ToolTip7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        ToolTip8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ToolTip8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        ToolTip9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ToolTip9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        Hover11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover11.setText("?");
        Hover11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        Hover12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover12.setText("?");
        Hover12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover12MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover12MouseExited(evt);
            }
        });

        Hover10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover10.setText("?");
        Hover10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover10MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover10MouseExited(evt);
            }
        });

        javax.swing.GroupLayout einSchuelerTabLayout = new javax.swing.GroupLayout(einSchuelerTab);
        einSchuelerTab.setLayout(einSchuelerTabLayout);
        einSchuelerTabLayout.setHorizontalGroup(
            einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einSchuelerTabLayout.createSequentialGroup()
                .addGap(100, 100, 100)
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
                                    .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(schuelerZurueck, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(einSchuelerTabLayout.createSequentialGroup()
                                            .addComponent(jLabel7)
                                            .addGap(81, 81, 81)
                                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(einSchuelerTabLayout.createSequentialGroup()
                                            .addGap(14, 14, 14)
                                            .addComponent(Hover10, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einSchuelerTabLayout.createSequentialGroup()
                .addGap(500, 500, 500)
                .addComponent(schuelerExport, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(schuelerExportPreisliste, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Hover12, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ausgeben, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(ausgebenIDFeld, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einSchuelerTabLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ausgebenKaufenFeld, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46))
            .addGroup(einSchuelerTabLayout.createSequentialGroup()
                .addGap(108, 108, 108)
                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(einSchuelerTabLayout.createSequentialGroup()
                        .addComponent(ToolTip9, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(einSchuelerTabLayout.createSequentialGroup()
                        .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ToolTip7, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ToolTip8, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buecherSchuelerTblAkt)
                        .addGap(344, 344, 344))))
            .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(einSchuelerTabLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(Hover11)
                    .addGap(0, 0, Short.MAX_VALUE)))
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
                                    .addComponent(Hover10))
                                .addGap(74, 74, 74)
                                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(schuelerZurueck)
                                    .addComponent(schuelerWeiter)))
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(buecherSchuelerTblAkt)
                    .addGroup(einSchuelerTabLayout.createSequentialGroup()
                        .addComponent(ToolTip7, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ToolTip8, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ToolTip9, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(schuelerExport)
                    .addComponent(ausgeben)
                    .addComponent(schuelerExportPreisliste)
                    .addComponent(ausgebenIDFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Hover12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ausgebenKaufenFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
            .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(einSchuelerTabLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(Hover11)
                    .addGap(0, 0, Short.MAX_VALUE)))
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

        einBuchISBNFeld.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        einBuchISBNFeld.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        einBuchKaufFeld.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        einBuchKaufFeld.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        einBuchPreisFeld.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        einBuchPreisFeld.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        einBuchLabelFeld.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        einBuchLabelFeld.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        einBuchLabelFeld.setText("- - -");

        Hover13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover13.setText("?");
        Hover13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover13MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover13MouseExited(evt);
            }
        });

        Hover14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover14.setText("?");
        Hover14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover14MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover14MouseExited(evt);
            }
        });

        Hover15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover15.setText("?");
        Hover15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover15MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover15MouseExited(evt);
            }
        });

        Hover16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover16.setText("?");
        Hover16.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover16MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover16MouseExited(evt);
            }
        });

        Hover17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover17.setText("?");
        Hover17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover17MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover17MouseExited(evt);
            }
        });

        Hover18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover18.setText("?");
        Hover18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover18MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover18MouseExited(evt);
            }
        });

        ToolTip10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ToolTip10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        ToolTip11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ToolTip11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        ToolTip12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ToolTip12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

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
                                .addComponent(Hover13, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel17))
                        .addGap(457, 1141, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einBuchTabLayout.createSequentialGroup()
                        .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, einBuchTabLayout.createSequentialGroup()
                                .addComponent(labelSuche, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Hover14, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                                    .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(einBuchPreisFeld, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(einBuchKaufFeld, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(einBuchISBNFeld, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)))))
                        .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(einBuchTabLayout.createSequentialGroup()
                                        .addGap(343, 343, 343)
                                        .addComponent(Hover17, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(buchBearbeiten))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einBuchTabLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(Hover18, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(buchLöschen, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einBuchTabLayout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(Hover16, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buchNeu, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(einBuchTabLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Hover15, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
            .addGroup(einBuchTabLayout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ToolTip12, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ToolTip11, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ToolTip10, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        einBuchTabLayout.setVerticalGroup(
            einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(einBuchTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isbnSuche, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Hover13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addGap(12, 12, 12)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSuche, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(einBuchLabelFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Hover14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel17)
                .addGap(41, 41, 41)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(einBuchISBNL)
                    .addComponent(buchNeu)
                    .addComponent(einBuchISBNFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Hover16))
                .addGap(41, 41, 41)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(buchBearbeiten)
                    .addComponent(einBuchKaufFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Hover17))
                .addGap(41, 41, 41)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(buchLöschen)
                    .addComponent(einBuchPreisFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Hover18))
                .addGap(33, 33, 33)
                .addComponent(ToolTip10, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ToolTip11, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ToolTip12, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(142, 142, 142)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(neuKopieBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Hover15))
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

        ToolTip1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ToolTip1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        Hover1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover1.setText("?");
        Hover1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover1MouseExited(evt);
            }
        });

        Hover2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover2.setText("?");
        Hover2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover2MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover2MouseExited(evt);
            }
        });

        Hover3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover3.setText("?");
        Hover3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover3MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover3MouseExited(evt);
            }
        });

        Hover4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Hover4.setText("?");
        Hover4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Hover4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                Hover4MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                Hover4MouseExited(evt);
            }
        });

        ToolTip2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ToolTip2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        ToolTip3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ToolTip3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        ToolTip4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ToolTip4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

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
                                .addComponent(Hover1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(einKopieTabLayout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(einKopieTabLayout.createSequentialGroup()
                                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(einKopieTabLayout.createSequentialGroup()
                                        .addGap(453, 453, 453)
                                        .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(Hover2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(Hover3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(ToolTip2, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ToolTip3, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ToolTip4, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(kopieLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(einKopieTabLayout.createSequentialGroup()
                                        .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(kopieEinsammeln, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(einKopieTabLayout.createSequentialGroup()
                                                .addComponent(kopieLöschen)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(Hover4, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                                .addComponent(ToolTip1, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(69, 69, 69)
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
                    .addComponent(Hover1))
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
                            .addComponent(Hover2))
                        .addGap(18, 18, 18)
                        .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(kopieLöschen)
                            .addComponent(kopieBarcodeErneut)
                            .addComponent(Hover3)
                            .addComponent(Hover4))
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
                                    .addComponent(kopiePaid, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ToolTip1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(4, 4, 4)
                                .addComponent(ToolTip2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ToolTip3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ToolTip4, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(103, Short.MAX_VALUE))))
        );

        basePanel.addTab("Einzelne Kopie", einKopieTab);

        einsammelnTab.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                einsammelnTabComponentAdded(evt);
            }
        });

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

        javax.swing.GroupLayout einsammelnTabLayout = new javax.swing.GroupLayout(einsammelnTab);
        einsammelnTab.setLayout(einsammelnTabLayout);
        einsammelnTabLayout.setHorizontalGroup(
            einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(einsammelnTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(einsammelnTabLayout.createSequentialGroup()
                        .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 935, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addComponent(einsammelnPic))
                    .addGroup(einsammelnTabLayout.createSequentialGroup()
                        .addComponent(einsammelnAlles, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(einsammelnTabLayout.createSequentialGroup()
                        .addComponent(einsammelnEingabe, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(einsammelnEintragLoeschen, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        einsammelnTabLayout.setVerticalGroup(
            einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einsammelnTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(einsammelnEingabe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(einsammelnEintragLoeschen, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(einsammelnTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 554, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(einsammelnPic))
                .addGap(18, 18, 18)
                .addComponent(einsammelnAlles, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        basePanel.addTab("Einsammeln", einsammelnTab);

        klassenTab.setPreferredSize(new java.awt.Dimension(1373, 672));
        klassenTab.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                klassenTabComponentAdded(evt);
            }
        });

        buchKlassenList.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        buchKlassenList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                buchKlassenListMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(buchKlassenList);

        buecherKlassenTbl.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        buecherKlassenTbl.setModel(new javax.swing.table.DefaultTableModel(
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
        buecherKlassenTbl.setDragEnabled(true);
        jScrollPane7.setViewportView(buecherKlassenTbl);

        javax.swing.GroupLayout klassenTabLayout = new javax.swing.GroupLayout(klassenTab);
        klassenTab.setLayout(klassenTabLayout);
        klassenTabLayout.setHorizontalGroup(
            klassenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(klassenTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 1157, Short.MAX_VALUE)
                .addContainerGap())
        );
        klassenTabLayout.setVerticalGroup(
            klassenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, klassenTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(klassenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, klassenTabLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane6))
                .addGap(72, 72, 72))
        );

        basePanel.addTab("Klassen", klassenTab);

        klassenBearbeitenTab.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                klassenBearbeitenTabComponentAdded(evt);
            }
        });

        buchKlassenList1.setFont(new java.awt.Font("Arial", 0, 20)); // NOI18N
        buchKlassenList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                buchKlassenList1MouseClicked(evt);
            }
        });
        jScrollPane9.setViewportView(buchKlassenList1);

        buecherInKlasseTbl.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane10.setViewportView(buecherInKlasseTbl);

        buecherFKlassenTbl.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane11.setViewportView(buecherFKlassenTbl);

        javax.swing.GroupLayout klassenBearbeitenTabLayout = new javax.swing.GroupLayout(klassenBearbeitenTab);
        klassenBearbeitenTab.setLayout(klassenBearbeitenTabLayout);
        klassenBearbeitenTabLayout.setHorizontalGroup(
            klassenBearbeitenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(klassenBearbeitenTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(klassenBearbeitenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 1157, Short.MAX_VALUE)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 1157, Short.MAX_VALUE))
                .addContainerGap())
        );
        klassenBearbeitenTabLayout.setVerticalGroup(
            klassenBearbeitenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(klassenBearbeitenTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(klassenBearbeitenTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(klassenBearbeitenTabLayout.createSequentialGroup()
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(klassenBearbeitenTabLayout.createSequentialGroup()
                        .addComponent(jScrollPane9)
                        .addGap(61, 61, 61))))
        );

        basePanel.addTab("Klassen Bearbeiten", klassenBearbeitenTab);

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
                                .addComponent(sumPrice))
                            .addGroup(exportTabLayout.createSequentialGroup()
                                .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(pdfExportOpAddAllesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pdfExportOpDelAllButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pdfExportOpAddSelectButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pdfExportOpDelSelectButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 237, Short.MAX_VALUE)
                        .addComponent(pdfExportProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(exportTabLayout.createSequentialGroup()
                            .addComponent(pdfExportAddAllesButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pdfExportAddSelectButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pdfExportDelSelectButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pdfExportDelAllButton)
                            .addGap(230, 230, 230))
                        .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 567, Short.MAX_VALUE)
                        .addComponent(jScrollPane12))
                    .addGroup(exportTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(exportTabLayout.createSequentialGroup()
                            .addComponent(pdfExportOpAddAllesButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pdfExportOpAddSelectButton2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pdfExportOpDelSelectButton2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pdfExportOpDelAllButton2)
                            .addGap(230, 230, 230))
                        .addComponent(jScrollPane18)
                        .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 567, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pdfExportProgressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        buchKlassenList1.setListData(names.toArray());
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

    private void buchKlassenListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buchKlassenListMouseClicked
        currentPanel = 4;
        int index = buchKlassenList.locationToIndex(evt.getPoint());
        ListModel dlm = buchKlassenList.getModel();
        buchKlassenList1.ensureIndexIsVisible(index);
        UpdateTable(BookGroups.BooksList(dlm.getElementAt(index).toString()));
    }//GEN-LAST:event_buchKlassenListMouseClicked

    private void klassenTabComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_klassenTabComponentAdded
        ArrayList<String> names = new ArrayList<>();
        for (String s : Classes.getClassIDs()) {
            names.add(Classes.getClassName(s));
        }
        buchKlassenList.setListData(names.toArray());
    }//GEN-LAST:event_klassenTabComponentAdded

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
        String buchISBN = isbnSuche.getText();
        if (Books.singleBook(buchISBN, 0).isEmpty() == true) {
            einBuchLabelFeld.setText("Kein Buch mit dieser ISBN");
            einBuchISBNFeld.setText("");
            einBuchKaufFeld.setText("");
            einBuchPreisFeld.setText("");
        } else {
            einBuchLabelFeld.setText(Books.singleBook(buchISBN, 0).get(0));
            einBuchISBNFeld.setText(Books.singleBook(buchISBN, 0).get(1));
            einBuchKaufFeld.setText(Books.singleBook(buchISBN, 0).get(3));
            einBuchPreisFeld.setText(Books.singleBook(buchISBN, 0).get(2));
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
        String buchLabel = isbnSuche.getText();
        if (Books.singleBook(buchLabel, 1).isEmpty() == true) {
            einBuchLabelFeld.setText("Kein Buch mir diesem Label");
            einBuchISBNFeld.setText("");
            einBuchKaufFeld.setText("");
            einBuchPreisFeld.setText("");
        } else {
            einBuchLabelFeld.setText(Books.singleBook(buchLabel, 1).get(0));
            einBuchISBNFeld.setText(Books.singleBook(buchLabel, 1).get(1));
            einBuchKaufFeld.setText(Books.singleBook(buchLabel, 1).get(3));
            einBuchPreisFeld.setText(Books.singleBook(buchLabel, 1).get(2));
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
            }
            for (int i = 0; i < anz; i++) {
                Copies.addCopy(Books.singleBook(einBuchISBNFeld.getText(), 0).get(4), id + i);
            }
        }
    }//GEN-LAST:event_neuKopieBtnMouseClicked

    private void schuelerKlassenListNeuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_schuelerKlassenListNeuMouseClicked
        int index = schuelerKlassenListNeu.locationToIndex(evt.getPoint());
        ListModel dlm = schuelerKlassenListNeu.getModel();
        schuelerKlassenListNeu.ensureIndexIsVisible(index);
        Students.addToClass(schuelerId, dlm.getElementAt(index).toString());
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
            Books.newBook(einBuchLabelFeld.getText(), einBuchISBNFeld.getText(), einBuchPreisFeld.getText(), einBuchKaufFeld.getText());
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

            Books.editBook(Books.singleBook(einBuchISBNFeld.getText(), 0).get(4), einBuchLabelFeld.getText(), einBuchISBNFeld.getText(), einBuchPreisFeld.getText(), einBuchKaufFeld.getText());
            buchBearbeiten.setText("Buch bearbeiten");
            buchNeu.setEnabled(true);
            buchLöschen.setEnabled(true);
            speichern = 0;
        }
    }//GEN-LAST:event_buchBearbeitenActionPerformed

    private void schuelerKlassenBearbeitenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schuelerKlassenBearbeitenActionPerformed
        if (skBearbeiten == 0) {
            schuelerKlassenListNeu.setEnabled(true);
            schuelerKlassenBearbeiten.setText("Speichern");
            skBearbeiten = 1;
        } else {
            schuelerKlassenListNeu.setEnabled(false);
            schuelerKlassenBearbeiten.setText("Bearbeiten");
            skBearbeiten = 0;
        }
    }//GEN-LAST:event_schuelerKlassenBearbeitenActionPerformed

    private void buchKlassenList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buchKlassenList1MouseClicked
        currentPanel = 5;
        int index = buchKlassenList1.locationToIndex(evt.getPoint());
        ListModel dlm = buchKlassenList1.getModel();
        buchKlassenList1.ensureIndexIsVisible(index);
        UpdateTable(BookGroups.BooksList(dlm.getElementAt(index).toString()));
    }//GEN-LAST:event_buchKlassenList1MouseClicked

    private void klassenBearbeitenTabComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_klassenBearbeitenTabComponentAdded
        currentPanel = 6;
        UpdateTable(Books.BookList());
    }//GEN-LAST:event_klassenBearbeitenTabComponentAdded

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
        } catch (IOException | DocumentException e) {
            System.out.println(e + " => Cant print Barcode of Copie");
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
            System.out.println(" => Input is not a number!");
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
            kopieClass.setText("");
        }

        try {
            PicEinzelneKopie.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sbv/pictures/Buch_"
                    + kopie.get(9)
                    + ".jpg")));
        } catch (Exception e) {
            System.out.println(e + " => Cant show BookPic");
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
    }//GEN-LAST:event_einsammelnEintragLoeschenActionPerformed

    private void einsammelnAllesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_einsammelnAllesActionPerformed
        while (einsammelTabelleModel.getRowCount() != 0) {
            Copies.collectCopy((String) einsammelTabelleModel.getValueAt(0, 2));
            einsammelTabelleModel.removeRow(0);
        }
        einsammelnAlles.setEnabled(false);
        einsammelnPic.setVisible(false);
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
            einsammelnPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sbv/pictures/missingPicture.png")));
        }
        einsammelnPic.setVisible(true);
    }//GEN-LAST:event_einsammelnTabelleMouseClicked

    private void einsammelnTabComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_einsammelnTabComponentAdded
        currentPanel = 8;
        UpdateTable(null);
    }//GEN-LAST:event_einsammelnTabComponentAdded

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

    private void Hover1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover1MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip1.setText("Hier Text einfügen");
        ToolTip2.setText("Hier Text einfügen");
        ToolTip3.setText("Hier Text einfügen");
        ToolTip4.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover1MouseEntered

    private void Hover1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover1MouseExited
        ToolTip1.setText("");
        ToolTip2.setText("");
        ToolTip3.setText("");
        ToolTip4.setText("");
    }//GEN-LAST:event_Hover1MouseExited

    private void Hover2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover2MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip1.setText("Hier Text einfügen");
        ToolTip2.setText("Hier Text einfügen");
        ToolTip3.setText("Hier Text einfügen");
        ToolTip4.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover2MouseEntered

    private void Hover2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover2MouseExited
        ToolTip1.setText("");
        ToolTip2.setText("");
        ToolTip3.setText("");
        ToolTip4.setText("");
    }//GEN-LAST:event_Hover2MouseExited

    private void Hover3MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover3MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip1.setText("Hier Text einfügen");
        ToolTip2.setText("Hier Text einfügen");
        ToolTip3.setText("Hier Text einfügen");
        ToolTip4.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover3MouseEntered

    private void Hover3MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover3MouseExited
        ToolTip1.setText("");
        ToolTip2.setText("");
        ToolTip3.setText("");
        ToolTip4.setText("");
    }//GEN-LAST:event_Hover3MouseExited

    private void Hover4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover4MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip1.setText("Hier Text einfügen");
        ToolTip2.setText("Hier Text einfügen");
        ToolTip3.setText("Hier Text einfügen");
        ToolTip4.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover4MouseEntered

    private void Hover4MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover4MouseExited
        ToolTip1.setText("");
        ToolTip2.setText("");
        ToolTip3.setText("");
        ToolTip4.setText("");
    }//GEN-LAST:event_Hover4MouseExited

    private void Hover5MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover5MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip5.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover5MouseEntered

    private void Hover5MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover5MouseExited
        ToolTip5.setText("");
    }//GEN-LAST:event_Hover5MouseExited

    private void Hover6MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover6MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip6.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover6MouseEntered

    private void Hover6MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover6MouseExited
        ToolTip6.setText("");
    }//GEN-LAST:event_Hover6MouseExited

    private void Hover7MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover7MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip6.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover7MouseEntered

    private void Hover7MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover7MouseExited
        ToolTip6.setText("");
    }//GEN-LAST:event_Hover7MouseExited

    private void Hover8MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover8MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip6.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover8MouseEntered

    private void Hover8MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover8MouseExited
        ToolTip6.setText("");
    }//GEN-LAST:event_Hover8MouseExited

    private void Hover9MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover9MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip6.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover9MouseEntered

    private void Hover9MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover9MouseExited
        ToolTip6.setText("");
    }//GEN-LAST:event_Hover9MouseExited

    private void Hover10MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover10MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip7.setText("Hier Text einfügen");
        ToolTip8.setText("Hier Text einfügen");
        ToolTip9.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover10MouseEntered

    private void Hover10MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover10MouseExited
        ToolTip7.setText("");
        ToolTip8.setText("");
        ToolTip9.setText("");
    }//GEN-LAST:event_Hover10MouseExited

    private void Hover12MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover12MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip7.setText("Hier Text einfügen");
        ToolTip8.setText("Hier Text einfügen");
        ToolTip9.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover12MouseEntered

    private void Hover12MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover12MouseExited
        ToolTip7.setText("");
        ToolTip8.setText("");
        ToolTip9.setText("");
    }//GEN-LAST:event_Hover12MouseExited

    private void Hover13MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover13MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip10.setText("Hier Text einfügen");
        ToolTip11.setText("Hier Text einfügen");
        ToolTip12.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover13MouseEntered

    private void Hover13MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover13MouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_Hover13MouseExited

    private void Hover14MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover14MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip10.setText("Hier Text einfügen");
        ToolTip11.setText("Hier Text einfügen");
        ToolTip12.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover14MouseEntered

    private void Hover14MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover14MouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_Hover14MouseExited

    private void Hover15MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover15MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip10.setText("Hier Text einfügen");
        ToolTip11.setText("Hier Text einfügen");
        ToolTip12.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover15MouseEntered

    private void Hover15MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover15MouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_Hover15MouseExited

    private void Hover16MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover16MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip10.setText("Hier Text einfügen");
        ToolTip11.setText("Hier Text einfügen");
        ToolTip12.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover16MouseEntered

    private void Hover16MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover16MouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_Hover16MouseExited

    private void Hover17MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover17MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip10.setText("Hier Text einfügen");
        ToolTip11.setText("Hier Text einfügen");
        ToolTip12.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover17MouseEntered

    private void Hover17MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover17MouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_Hover17MouseExited

    private void Hover18MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover18MouseEntered
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e + " => Hover");
        }
        ToolTip10.setText("Hier Text einfügen");
        ToolTip11.setText("Hier Text einfügen");
        ToolTip12.setText("Hier Text einfügen");
    }//GEN-LAST:event_Hover18MouseEntered

    private void Hover18MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Hover18MouseExited
        ToolTip10.setText("");
        ToolTip11.setText("");
        ToolTip12.setText("");
    }//GEN-LAST:event_Hover18MouseExited

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
    private javax.swing.JLabel Hover1;
    private javax.swing.JLabel Hover10;
    private javax.swing.JLabel Hover11;
    private javax.swing.JLabel Hover12;
    private javax.swing.JLabel Hover13;
    private javax.swing.JLabel Hover14;
    private javax.swing.JLabel Hover15;
    private javax.swing.JLabel Hover16;
    private javax.swing.JLabel Hover17;
    private javax.swing.JLabel Hover18;
    private javax.swing.JLabel Hover2;
    private javax.swing.JLabel Hover3;
    private javax.swing.JLabel Hover4;
    private javax.swing.JLabel Hover5;
    private javax.swing.JLabel Hover6;
    private javax.swing.JLabel Hover7;
    private javax.swing.JLabel Hover8;
    private javax.swing.JLabel Hover9;
    private javax.swing.JLabel PicEinzelneKopie;
    private javax.swing.JLabel ToolTip1;
    private javax.swing.JLabel ToolTip10;
    private javax.swing.JLabel ToolTip11;
    private javax.swing.JLabel ToolTip12;
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
    private javax.swing.JButton buchBearbeiten;
    public static javax.swing.JList buchKlassenList;
    public static javax.swing.JList buchKlassenList1;
    private javax.swing.JButton buchLöschen;
    private javax.swing.JButton buchNeu;
    private javax.swing.JRadioButton buchRadioButton;
    private javax.swing.JTable buecherFKlassenTbl;
    private javax.swing.JTable buecherInKlasseTbl;
    private javax.swing.JTable buecherKlassenTbl;
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
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JButton klasseExportBtn;
    private javax.swing.JButton klasseExportPreislist;
    private javax.swing.JRadioButton klasseRadioButton;
    private javax.swing.JPanel klassenBearbeitenTab;
    public static javax.swing.JList klassenList;
    private javax.swing.JPanel klassenTab;
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
