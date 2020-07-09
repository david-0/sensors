package org.sensors.logic;

public enum ButtonState {
	ON(true), OFF(false);
	
	private boolean contactClosed;

	private ButtonState(boolean contactClosed) {
		this.contactClosed = contactClosed;
	}
	
	public boolean isContactClosed() {
		return contactClosed;
	}
}
