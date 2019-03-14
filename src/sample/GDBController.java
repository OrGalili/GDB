package sample;

public class GDBController {
    private GDBModel model;
    private View view; // How to connect with FXML?

    public GDBController(GDBModel m, View v) {
        model = m;
        view = v;
    }

    public void initController() { // Components name when defined in XML?
        // Events
        view.getFirstnameSaveButton().addActionListener(e -> saveFirstname());
        view.getLastnameSaveButton().addActionListener(e -> saveLastname());
        view.getHello().addActionListener(e -> sayHello());
        view.getBye().addActionListener(e -> sayBye());
    }

    private void saveFirstname() {
        // Event Handler
        model.setFirstname(view.getFirstnameTextfield().getText());
        JOptionPane.showMessageDialog(null, "Firstname saved : " + model.getFirstname(), "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
