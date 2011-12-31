package net.sourceforge.ganttproject.document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import net.sourceforge.ganttproject.GPLogger;
import net.sourceforge.ganttproject.gui.options.model.StringOption;

public class FtpDocument extends AbstractURLDocument implements Document {
    private static final Object EMPTY_STRING = "";
    private final URI myURI;

    FtpDocument(String urlAsString, StringOption ftpUser, StringOption ftpPassword) {
        assert urlAsString!=null;
        try {
            URI url = new URI(urlAsString);
            String userInfo = url.getUserInfo();
            if (userInfo==null || EMPTY_STRING.equals(userInfo)) {
                StringBuffer buf = new StringBuffer();
                if (ftpUser.getValue()!=null) {
                    buf.append(ftpUser.getValue());
                }
                if (ftpPassword.getValue()!=null) {
                    buf.append(':').append(ftpPassword.getValue());
                }
                myURI = new URI("ftp", buf.toString(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getFragment());
            }
            else {
                myURI = url;
            }
            urlAsString = myURI.toString();
            myURI.toURL().openConnection().connect();
        } catch (URISyntaxException e) {
            if (!GPLogger.log(e)) {
                e.printStackTrace(System.err);
            }
            throw new RuntimeException("Failed to create FTP document addressed by URL="+urlAsString, e);
        } catch (MalformedURLException e) {
            if (!GPLogger.log(e)) {
                e.printStackTrace();
            }
            throw new RuntimeException("Failed to create FTP document addressed by URL="+urlAsString, e);
        } catch (IOException e) {
            if (!GPLogger.log(e)) {
                e.printStackTrace();
            }
            throw new RuntimeException("Failed to create FTP document addressed by URL="+urlAsString, e);
        }
    }

    @Override
    public String getFileName() {
        //TODO return filename instead of complete URI?
        return myURI.toString();
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public IStatus canWrite() {
        return Status.OK_STATUS;
    }

    @Override
    public boolean isValidForMRU() {
        return true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return myURI.toURL().openConnection().getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return myURI.toURL().openConnection().getOutputStream();
    }

    @Override
    public String getPath() {
        return myURI.toString();
    }

    @Override
    public void write() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getURI() {
        return myURI;
    }

    @Override
    public boolean isLocal() {
        return false;
    }


}
