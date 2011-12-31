package net.sourceforge.ganttproject.document;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.eclipse.core.runtime.IStatus;

import net.sourceforge.ganttproject.action.OkAction;
import net.sourceforge.ganttproject.export.WebPublisher;
import net.sourceforge.ganttproject.gui.options.OptionPageProviderBase;
import net.sourceforge.ganttproject.gui.options.OptionsPageBuilder;
import net.sourceforge.ganttproject.gui.options.OptionsPageBuilder.I18N;
import net.sourceforge.ganttproject.gui.options.model.ChangeValueEvent;
import net.sourceforge.ganttproject.gui.options.model.ChangeValueListener;
import net.sourceforge.ganttproject.gui.options.model.DefaultStringOption;
import net.sourceforge.ganttproject.gui.options.model.GPOptionGroup;
import net.sourceforge.ganttproject.language.GanttLanguage;

public class NetworkOptionPageProvider extends OptionPageProviderBase {

    public NetworkOptionPageProvider() {
        super("impex.ftp");
    }

    @Override
    public GPOptionGroup[] getOptionGroups() {
        return getProject().getDocumentManager().getNetworkOptionGroups();
    }

    @Override
    public boolean hasCustomComponent() {
        return true;
    }

    @Override
    public Component buildPageComponent() {
        OptionsPageBuilder builder = new OptionsPageBuilder();
        final GPOptionGroup ftpGroup = getProject().getDocumentManager().getNetworkOptionGroups()[0];
        ftpGroup.setTitled(false);
        I18N i18n = new OptionsPageBuilder.I18N();

        final DefaultStringOption usernameOption = (DefaultStringOption) ftpGroup.getOption(DocumentCreator.USERNAME_OPTION_ID);
        ftpGroup.setI18Nkey(i18n.getCanonicalOptionLabelKey(usernameOption), "ftpuser");

        final DefaultStringOption servernameOption = (DefaultStringOption) ftpGroup.getOption(DocumentCreator.SERVERNAME_OPTION_ID);
        ftpGroup.setI18Nkey(i18n.getCanonicalOptionLabelKey(servernameOption), "ftpserver");

        final DefaultStringOption dirnameOption = (DefaultStringOption) ftpGroup.getOption(DocumentCreator.DIRECTORYNAME_OPTION_ID);
        ftpGroup.setI18Nkey(i18n.getCanonicalOptionLabelKey(dirnameOption), "ftpdirectory");

        final DefaultStringOption passwordOption = (DefaultStringOption) ftpGroup.getOption(DocumentCreator.PASSWORD_OPTION_ID);
        ftpGroup.setI18Nkey(i18n.getCanonicalOptionLabelKey(passwordOption), "ftppwd");


        final JComponent optionsPane = builder.buildPage(getProject().getDocumentManager().getNetworkOptionGroups(), getPageID());
        final Action testConnectionAction = new AbstractAction() {
            {
                putValue(Action.NAME, GanttLanguage.getInstance().getText("testFTPConnection"));
                setEnabled(canEnableTestAction(ftpGroup));
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                WebPublisher.Ftp ftp = new WebPublisher.Ftp();
                try {
                    IStatus status = ftp.loginAndChangedir(getProject().getDocumentManager().getFTPOptions());
                    if (status.isOK()) {
                        getUiFacade().showOptionDialog(JOptionPane.INFORMATION_MESSAGE,
                                GanttLanguage.getInstance().getText("successFTPConnection"),
                                new Action [] {
                                    OkAction.createVoidAction("ok")
                                });
                    } else {
                        getUiFacade().showErrorDialog(status.getMessage());
                    }
                } catch (IOException e1) {
                    getUiFacade().showErrorDialog(e1);
                }
            }
        };
        ChangeValueListener listener = new ChangeValueListener() {
            @Override
            public void changeValue(ChangeValueEvent event) {
                testConnectionAction.setEnabled(canEnableTestAction(ftpGroup));
            }
        };
        servernameOption.addChangeValueListener(listener);
        usernameOption.addChangeValueListener(listener);
        passwordOption.addChangeValueListener(listener);
        JPanel result = new JPanel(new BorderLayout());
        result.setBorder(new EmptyBorder(5, 5, 5, 5));
        result.add(optionsPane, BorderLayout.NORTH);
        JButton testConnectionButton = new JButton(testConnectionAction);
        testConnectionButton.setAlignmentX(SwingConstants.RIGHT);

        JPanel connectionWrapper = new JPanel(new BorderLayout());
        connectionWrapper.add(testConnectionButton, BorderLayout.NORTH);

        result.add(connectionWrapper, BorderLayout.CENTER);
        return result;
    }

    private boolean canEnableTestAction(GPOptionGroup ftpGroup) {
        final DefaultStringOption usernameOption = (DefaultStringOption) ftpGroup.getOption(DocumentCreator.USERNAME_OPTION_ID);
        final DefaultStringOption servernameOption = (DefaultStringOption) ftpGroup.getOption(DocumentCreator.SERVERNAME_OPTION_ID);
        final DefaultStringOption passwordOption = (DefaultStringOption) ftpGroup.getOption(DocumentCreator.PASSWORD_OPTION_ID);
        return servernameOption.getValue()!=null && usernameOption.getValue()!=null && passwordOption.getValue()!=null;
    }

}
