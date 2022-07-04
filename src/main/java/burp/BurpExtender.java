package burp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class BurpExtender implements IBurpExtender,IContextMenuFactory{
    private PrintWriter stdout;
    private IExtensionHelpers helpers;
    private IBurpExtenderCallbacks callbacks;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        callbacks.setExtensionName("ParameterAnalysis");
        callbacks.registerContextMenuFactory(this);
        this.stdout = new PrintWriter(callbacks.getStdout(), true);
        this.helpers = callbacks.getHelpers();
    }

    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        ArrayList<JMenuItem> jMenuItems = new ArrayList();
        JMenuItem jMenuItem = new JMenuItem("ParameterAnalysis");
        jMenuItems.add(jMenuItem);
        jMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IHttpRequestResponse[] messages = invocation.getSelectedMessages();
                HttpRequest httpRequest = new HttpRequest(helpers, stdout, callbacks, messages);
                Thread thread = new Thread(httpRequest);
                thread.start();
            }
        });
        return jMenuItems;
    }
}
