//monitorgröße : 1366 * 768
//fenstergröße : 1382 * 784
package sbv;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private static final String pdfExportSchuelerOpList[] = {"Label", "Gekauft", "Ausgegeben", "Bezahlt", "Code"}; //label, buy, distributed, paid, sbm_copies.ID
    private static final String pdfExportClassOpList[] = {"Vorname", "Nachname", "Geburtstag", "Schüler-ID"}; //forename, surname, birth, student_ID
    private static final String pdfExportBookOpList[] = {"Label", "ISBN", "Preis", "Kauf", "ID", "Anz. Kopien", "Anz. Ausgegeben", "Anz. Lager"}; //label, isbn, price, buy, ID
    DefaultListModel pdfExportOpAllesModel = new DefaultListModel();
    DefaultListModel pdfExportOpSelectModel = new DefaultListModel();

    //static private ArrayList<String> names;
    //static private int index;
    //static private ArrayList<String> klasse;
    //static private ListModel dlm;
//    static private Object item;
//    static private ArrayList<String> data;
//    static private ArrayList<String> names1;
//    static private String buecherKlasse;
//    static private ArrayList<String> buchKlasse;
//    static private String buchISBN;
//    static private ArrayList<String> ids;
//    static private int buecherRow;
//    static private String buchLabel;
//    static private int anz;
//    static private int id;
//    static private String barcode;
//    static private String classe;
    static private String momentaneKopie;
//    static private int testInteger;
//    static private ArrayList<String> kopie;
    static private String schuelerId;
    static private int schuelerInKlasse;
    static private int schuelerRow;
    static private TableColumn col;
    static private String momentaneKlasse = null;
    static private int currentPanel = 1;
    static private int speichern = 0;
    static private int skBearbeiten = 0;
//    static private Object[] input;
//    static private final ArrayList doppelt = new ArrayList();
//    static private int[] selected;
//    static private PdfPTable table;
//    static private int width, heigth;

    static private String user;
    static private int lizenz;
    static private final String[] lizenzenNamen = {"lokaler Admin", "Admin", "Sekretär", "Lehrkraft"};

    Connection conn = null;

    private Thread exp;
    Runnable ex = new Runnable() {
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

                        pdfExportProgressBar.setString((String) pdfExportAuswahlSelectListModel.getElementAt(i));//?
                        pdfExportProgressBar.setValue(i);
//*****                    
                        if (schuelerRadioButton.isSelected()) {
                            schuelerId = Students.StudentSearch((String) pdfExportAuswahlSelectListModel.getElementAt(i));
                            document.add(schuelerEx(schuelerId));
                            document.add(PDF_Export.pdfChapterStudent(schuelerId));
//*****                        
                        } else if (klasseRadioButton.isSelected()) {
                            String classe = (String) pdfExportAuswahlSelectListModel.getElementAt(i);
                            document.add(classEx(classe));
                            document.add(PDF_Export.pdfChapterClass(classe));
//*****
                        } else if (buchRadioButton.isSelected()) {
                            i = pdfExportAuswahlSelectListModel.size();
                            document.add(bookEx());
                            document.add(PDF_Export.pdfChapterBook());
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

                        pdfExportProgressBar.setString((String) pdfExportAuswahlSelectListModel.getElementAt(i));//?
                        pdfExportProgressBar.setValue(i);

                        final Document document = new Document(PageSize.A4);

                        System.out.println(pdfExportAuswahlSelectListModel.getElementAt(i));
                        System.out.println(pathName2 + "\\" + pdfExportAuswahlSelectListModel.getElementAt(i).toString().replace(" ", "-") + ".pdf");

                        writer = PdfWriter.getInstance(document, new FileOutputStream(pathName2 + "\\" + pdfExportAuswahlSelectListModel.getElementAt(i).toString().replace(" ", "-") + ".pdf"));

                        document.addAuthor(System.getProperty("user.name"));
                        document.addCreationDate();
                        document.addCreator("Seminarkurs Programm Schulbuchverwaltung");
                        document.addTitle("PDF-Export von " + pdfExportAuswahlSelectListModel.getElementAt(i));

                        document.open();
//*****                    
                        if (schuelerRadioButton.isSelected()) {
                            schuelerId = Students.StudentSearch((String) pdfExportAuswahlSelectListModel.getElementAt(i));
                            document.add(schuelerEx(schuelerId));
                            document.add(PDF_Export.pdfChapterStudent(schuelerId));
//*****                    
                        } else if (klasseRadioButton.isSelected()) {
                            String classe = (String) pdfExportAuswahlSelectListModel.getElementAt(i);
                            document.add(classEx(classe));
                            document.add(PDF_Export.pdfChapterClass(classe));
//*****                    
                        } else {
                            Other.errorWin("Fatal Error");
                            return;
                        }

                        document.close();
                        writer.close();
                    }

                    pdfExportProgressBar.setValue(pdfExportAuswahlSelectListModel.size());
                    pdfExportProgressBar.setString("Fertig");//?
                    pdfExportPrint.setEnabled(true);

                    /**
                     * *************************************************
                     */
                } else {
                    Other.errorWin("Fatal Error");
                    return;
                }

                pdfExportProgressBar.setValue(pdfExportAuswahlSelectListModel.size());
                pdfExportProgressBar.setString("Fertig");//?
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
        //this.pdfExportSchuelerOperationsModel = new DefaultListModel(pdfExportSchuelerOperations, 0);
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
        jButton2 = new javax.swing.JButton();
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 282, Short.MAX_VALUE)
                .addComponent(lizenzName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        jButton2.setText("Fake Preisliste");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout schuelerTabLayout = new javax.swing.GroupLayout(schuelerTab);
        schuelerTab.setLayout(schuelerTabLayout);
        schuelerTabLayout.setHorizontalGroup(
            schuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addGap(18, 18, 18)
                        .addComponent(klasseExportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(klasseExportPreislist, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                        .addComponent(neuKlasseBtn))
                    .addGroup(schuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(klasseExportPreislist)
                        .addComponent(klasseExportBtn)
                        .addComponent(jButton2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(schuelerWeiter, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(einSchuelerTabLayout.createSequentialGroup()
                                            .addGap(36, 36, 36)
                                            .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(schuelerKlassenBearbeiten, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jScrollPane5))))))
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
                .addComponent(ausgeben, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(ausgebenIDFeld, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einSchuelerTabLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einSchuelerTabLayout.createSequentialGroup()
                        .addComponent(ausgebenKaufenFeld, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einSchuelerTabLayout.createSequentialGroup()
                        .addComponent(buecherSchuelerTblAkt)
                        .addGap(344, 344, 344))))
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
                                .addComponent(schuelerKlassenBearbeiten)
                                .addGap(74, 74, 74)
                                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(schuelerZurueck)
                                    .addComponent(schuelerWeiter)))
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buecherSchuelerTblAkt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(einSchuelerTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(schuelerExport)
                    .addComponent(ausgeben)
                    .addComponent(schuelerExportPreisliste)
                    .addComponent(ausgebenIDFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ausgebenKaufenFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
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
                            .addComponent(isbnSuche, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einBuchTabLayout.createSequentialGroup()
                        .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, einBuchTabLayout.createSequentialGroup()
                                .addComponent(labelSuche, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(260, 260, 260)
                                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(einBuchTabLayout.createSequentialGroup()
                                        .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(einBuchISBNL, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 0, Short.MAX_VALUE))
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
                                .addGroup(einBuchTabLayout.createSequentialGroup()
                                    .addGap(365, 365, 365)
                                    .addComponent(buchBearbeiten))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einBuchTabLayout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buchLöschen, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, einBuchTabLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buchNeu, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        einBuchTabLayout.setVerticalGroup(
            einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(einBuchTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(isbnSuche, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addGap(12, 12, 12)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSuche, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(einBuchLabelFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel17)
                .addGap(41, 41, 41)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(einBuchISBNL)
                    .addComponent(buchNeu)
                    .addComponent(einBuchISBNFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(buchBearbeiten)
                    .addComponent(einBuchKaufFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41)
                .addGroup(einBuchTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(buchLöschen)
                    .addComponent(einBuchPreisFeld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(neuKopieBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        javax.swing.GroupLayout einKopieTabLayout = new javax.swing.GroupLayout(einKopieTab);
        einKopieTab.setLayout(einKopieTabLayout);
        einKopieTabLayout.setHorizontalGroup(
            einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, einKopieTabLayout.createSequentialGroup()
                    .addGap(400, 400, 400)
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
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(485, 485, 485))
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, einKopieTabLayout.createSequentialGroup()
                    .addGap(475, 475, 475)
                    .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(kopieLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(einKopieTabLayout.createSequentialGroup()
                            .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(kopieEinsammeln, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(einKopieTabLayout.createSequentialGroup()
                                    .addGap(58, 58, 58)
                                    .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(kopieDistributed, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                        .addComponent(kopieBought, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(kopiePaid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGroup(einKopieTabLayout.createSequentialGroup()
                                    .addComponent(kopieLöschen)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(kopieBarcodeErneut)))
                            .addGap(18, 18, 18)
                            .addComponent(PicEinzelneKopie)))
                    .addGap(292, 292, 292)))
            .addGroup(einKopieTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(eineKopieSuchen, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(217, 217, 217))
        );
        einKopieTabLayout.setVerticalGroup(
            einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(einKopieTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(eineKopieSuchen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(jLabel15)
                .addGap(1, 1, 1)
                .addComponent(kopieLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                            .addGroup(einKopieTabLayout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(kopiePaid, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(kopieEinsammeln, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(einKopieTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(kopieLöschen)
                            .addComponent(kopieBarcodeErneut))
                        .addGap(179, 179, 179))
                    .addGroup(einKopieTabLayout.createSequentialGroup()
                        .addComponent(PicEinzelneKopie)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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

        pdfExportAuswahlAllesList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
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

        pdfExportAuswahlSelectList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane14.setViewportView(pdfExportAuswahlSelectList);

        pdfExportDelSelectButton.setText("<");
        pdfExportDelSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfExportDelSelectButtonActionPerformed(evt);
            }
        });

        superSelectComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        superSelectComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                superSelectComboBoxActionPerformed(evt);
            }
        });

        pdfExportOpAllesList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
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

        pdfExportOpSelectList2.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
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
        groupExport.setText("zusammen");

        numCheckBox.setSelected(true);
        numCheckBox.setText("Nummerierung");

        pdfExportProgressBar.setMaximum(10);
        pdfExportProgressBar.setToolTipText("");
        pdfExportProgressBar.setStringPainted(true);

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
                            .addComponent(numCheckBox)
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
                    .addComponent(numCheckBox))
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
        PDF_Export.studentPDF(schuelerId, this);
        try {
            PDF_Export.openPDF();
        } catch (IOException e) {
            Logger.getLogger(Oberflaeche.class.getName()).log(Level.SEVERE, null, e);
        }
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
        schuelerKlassenList.setListData(Students.SingelStudentClasses(schuelerId).toArray());//ERROR

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
        PDF_Export.studentClassPDF(momentaneKlasse, this);
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
            neuKopieBtn.setText("Error");
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
        //Kopie in DB ausgeben
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
        PDF_Export.studentBill(schuelerId, this);
        try {
            PDF_Export.openPDF();
        } catch (IOException e) {
            Logger.getLogger(Oberflaeche.class.getName()).log(Level.SEVERE, null, e);
        }
    }//GEN-LAST:event_schuelerExportPreislisteMouseClicked

    private void klasseExportPreislistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_klasseExportPreislistActionPerformed
        PDF_Export.classBill(momentaneKlasse, this);
    }//GEN-LAST:event_klasseExportPreislistActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        PDF_Export.classBillFake(momentaneKlasse, this);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void kopieBarcodeErneutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kopieBarcodeErneutActionPerformed
        try {
            PDF_Export.barcodePDF(Integer.parseInt(momentaneKopie), 1);
        } catch (IOException | DocumentException e) {
            Logger.getLogger(Oberflaeche.class.getName()).log(Level.SEVERE, null, e);
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
        System.out.println(this.getSize());//get Size of Window
    }//GEN-LAST:event_einsammelnTabelleMouseClicked

    private void einsammelnTabComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_einsammelnTabComponentAdded
        currentPanel = 8;
        UpdateTable(null);
    }//GEN-LAST:event_einsammelnTabComponentAdded

    private void schuelerRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schuelerRadioButtonActionPerformed
        pdfExportAuswahlSelectListModel.clear();
        pdfExportAuswahlAllesListModel.clear();
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
        pdfExportAuswahlAllesListModel.removeAllElements();

        if (schuelerRadioButton.isSelected()) {
            ArrayList<String> data = Classes.classList((String) superSelectComboBox.getSelectedItem());
            for (int i = 0; i < data.size(); i = i + 4) {
                String n1 = data.get(i);
                String n2 = data.get(i + 1);
                pdfExportAuswahlAllesListModel.addElement(new StringBuilder(n1).append(" ").append(n2).toString());
//                System.out.println(data.get(i));
//                System.out.println(n2);
//                System.out.println(data.get(i + 1));
//                System.out.println(n1);
//                System.out.println(n2 + n1);
//                System.out.println(n2.concat(n1));
//                System.out.println(new StringBuilder(n2).append(n1));
//                System.out.println(new StringBuilder(n2).append(n1));
//                System.out.println(pdfExportAuswahlAllesListModel.get(pdfExportAuswahlAllesListModel.getSize() - 1));
//                System.out.println("-----");
            }

        } else if (klasseRadioButton.isSelected()) {

        } else if (buchRadioButton.isSelected()) {

        }

        pdfExportAuswahlAllesList.setModel(pdfExportAuswahlAllesListModel);
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
        int [] selected = pdfExportAuswahlAllesList.getSelectedIndices();
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
        int [] selected = pdfExportOpAllesList.getSelectedIndices();
        int id = 0;
        while (id < selected.length) {
            pdfExportOpSelectModel.addElement(pdfExportOpAllesModel.getElementAt(selected[id]));
            id++;
        }
        checkForDoublesOp();
    }//GEN-LAST:event_pdfExportOpAddSelectButton2ActionPerformed

    private void pdfExportOpDelAllButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfExportOpDelAllButton2ActionPerformed
        pdfExportOpSelectModel.removeAllElements();
    }//GEN-LAST:event_pdfExportOpDelAllButton2ActionPerformed

    private void pdfExportOpDelSelectButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfExportOpDelSelectButton2ActionPerformed
        while (pdfExportOpSelectList2.getSelectedIndex() != -1) {
            pdfExportOpSelectModel.remove(pdfExportOpSelectList2.getSelectedIndex());
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
        pdfExportProgressBar.setString("0%");
        pdfExportProgressBar.setMaximum(pdfExportAuswahlSelectListModel.size());
        pdfExportPrint.setEnabled(false);

        exp = new Thread(ex);
        exp.start();
    }//GEN-LAST:event_pdfExportPrintActionPerformed

    private void klasseRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_klasseRadioButtonActionPerformed
        pdfExportAuswahlSelectListModel.clear();
        pdfExportAuswahlAllesListModel.clear();
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

    private PdfPTable schuelerEx(String studentID) {
        ArrayList<String> source = Students.BookList(studentID); //label, buy, distributed, paid, sbm_copies.ID

        int width = pdfExportOpSelectModel.size();
        int heigth = source.size() / pdfExportOpAllesModel.size();

        PdfPTable table;
        if (numCheckBox.isSelected()) {
            table = new PdfPTable(width + 1);
            table.addCell(new PdfPCell(new Phrase("N", FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
        } else {
            table = new PdfPTable(width);
        }

        table.setSpacingBefore(25);
        table.setSpacingAfter(25);

        //Überschriften
        for (int i = 0; i < width; i++) {
            table.addCell(new PdfPCell(new Phrase((String) pdfExportOpSelectModel.get(i), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
        }

        //Inhalt
        for (int i = 0; i < heigth; i++) {
            if (numCheckBox.isSelected()) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
            }
            for (int j = 0; j < width; j++) {
                Object item = pdfExportOpSelectModel.getElementAt(j);
                if (item.equals(pdfExportSchuelerOpList[0])) {  //label
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * pdfExportSchuelerOpList.length + 0), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportSchuelerOpList[1])) {  //buy
//                    if (buecher.get(i * 5 + 1).equals("1")) {
//                        try {
//                            table.addCell(new PdfPCell(Image.getInstance("C:\\Users\\Falko\\Desktop"), true));
//                            //PicEinzelneKopie.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sbv/pictures/missingPicture.png")));
//                        } catch (BadElementException | IOException e) {
//                            System.out.println(e + " => schuelerEx - Pic ok1");
//                        }
//                    } else {
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * pdfExportSchuelerOpList.length + 1), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
//                    }
                    continue;
                }
                if (item.equals(pdfExportSchuelerOpList[2])) {  //distributed
                    table.addCell(new PdfPCell(new Phrase(Other.dateToNormal((String) source.get(i * pdfExportSchuelerOpList.length + 2)), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportSchuelerOpList[3])) {  //paid
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * pdfExportSchuelerOpList.length + 3), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportSchuelerOpList[4])) {  //code
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * pdfExportSchuelerOpList.length + 4), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }

                Other.errorWin("Fatal Error");

//                for (int k = 0; k < pdfExportOpAllesModel.size(); k++) {
//                    if (pdfExportOpSelectModel.getElementAt(j).equals(pdfExportSchuelerOpList[k])) {
//                        table.addCell(new PdfPCell(new Phrase((String) buecher.get(i * 5 + j), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
//                        break;
//                    }
//                }
            }
        }

        return table;
    }

    private PdfPTable classEx(String classID) {
        ArrayList<String> source = Classes.classList(classID); //forename, surname, birth, student_ID
        System.out.println(source);

        int width = pdfExportOpSelectModel.size();
        int heigth = source.size() / pdfExportOpAllesModel.size();

        PdfPTable table;
        if (numCheckBox.isSelected()) {
            table = new PdfPTable(width + 1);
            table.addCell(new PdfPCell(new Phrase("N", FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
        } else {
            table = new PdfPTable(width);
        }

        table.setSpacingBefore(25);
        table.setSpacingAfter(25);

        //Überschriften
        for (int i = 0; i < width; i++) {
            table.addCell(new PdfPCell(new Phrase((String) pdfExportOpSelectModel.get(i), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
        }

        //Inhalt
        for (int i = 0; i < heigth; i++) {
            if (numCheckBox.isSelected()) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
            }
            for (int j = 0; j < width; j++) {
                Object item = pdfExportOpSelectModel.getElementAt(j);
                if (item.equals(pdfExportClassOpList[0])) {  //label
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * pdfExportClassOpList.length + 0), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportClassOpList[1])) {  //surname
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * pdfExportClassOpList.length + 1), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportClassOpList[2])) {  //birth
                    table.addCell(new PdfPCell(new Phrase(Other.dateToNormal((String) source.get(i * pdfExportClassOpList.length + 2)), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportClassOpList[3])) {  //student_ID
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * pdfExportClassOpList.length + 3), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
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

        System.out.println(source);

        int width = pdfExportOpSelectModel.size();
        int heigth = source.size() / pdfExportOpAllesModel.size();

        PdfPTable table;
        if (numCheckBox.isSelected()) {
            table = new PdfPTable(width + 1);
            table.addCell(new PdfPCell(new Phrase("N", FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
        } else {
            table = new PdfPTable(width);
        }

        table.setSpacingBefore(25);
        table.setSpacingAfter(25);

        //Überschriften
        for (int i = 0; i < width; i++) {
            table.addCell(new PdfPCell(new Phrase((String) pdfExportOpSelectModel.get(i), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
        }

        //Inhalt
        for (int i = 0; i < heigth; i++) {
            if (numCheckBox.isSelected()) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
            }
            for (int j = 0; j < width; j++) {
                Object item = pdfExportOpSelectModel.getElementAt(j);
                String bookId = source.get(i * pdfExportBookOpList.length + 4);
                if (item.equals(pdfExportBookOpList[0])) {  //label
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * pdfExportBookOpList.length + 0), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[1])) {  //isbn
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * pdfExportBookOpList.length + 1), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[2])) {  //price
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * pdfExportBookOpList.length + 2), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[3])) {  //buy
                    table.addCell(new PdfPCell(new Phrase((String) source.get(i * pdfExportBookOpList.length + 3), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[4])) {  //ID
                    table.addCell(new PdfPCell(new Phrase(bookId, FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[5])) {  //ges
                    table.addCell(new PdfPCell(new Phrase(Copies.SingleCopyCountTotal(bookId), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[6])) {  //aus
                    System.out.println(Copies.SingleCopyCountTotal(bookId));
                    System.out.println(Integer.parseInt(Copies.SingleCopyCountTotal(bookId)));
                    int ges = Integer.parseInt(Copies.SingleCopyCountTotal(bookId));
                    System.out.println(ges);
                    int lager = Integer.parseInt(Copies.copiesInStock(bookId));
                    System.out.println(lager);
                    int aus = ges - lager;
                    System.out.println(aus);
                    table.addCell(new PdfPCell(new Phrase(String.valueOf(aus), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }
                if (item.equals(pdfExportBookOpList[7])) {  //lager
                    table.addCell(new PdfPCell(new Phrase(Copies.copiesInStock(bookId), FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD))));
                    continue;
                }

                Other.errorWin("Fatal Error");
            }
        }

        return table;
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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Oberflaeche.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
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
    private javax.swing.JLabel PicEinzelneKopie;
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
    private javax.swing.JButton jButton2;
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
    private javax.swing.JComboBox superSelectComboBox;
    private javax.swing.JLabel welcome;
    // End of variables declaration//GEN-END:variables
}
