
package org.musxav.jdbc;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class ContainerSSHSession {

	JSch jsch = null;
	Session sshSession = null;
	String sKey = null;
	int totalCons = 0;

	public ContainerSSHSession(JSch jsch, Session sshSession, String sKey) {

		this.jsch = jsch;
		this.sshSession = sshSession;
		this.sKey = sKey;
	}

	public JSch getJsch() {

		return this.jsch;
	}

	public void setJsch(JSch jsch) {

		this.jsch = jsch;
	}

	public void setSshSession(Session sshSession) {

		this.sshSession = sshSession;
	}

	public Session getSshSession() {

		return this.sshSession;
	}

	public String getKey() {

		return this.sKey;
	}

	public synchronized void connCreated() {

		++this.totalCons;
	}

	public synchronized void connClosed() {

		if (--this.totalCons < 1) {
			this.sshSession.disconnect();
		}
	}

}
