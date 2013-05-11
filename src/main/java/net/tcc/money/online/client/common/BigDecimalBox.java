package net.tcc.money.online.client.common;

import java.math.BigDecimal;


import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.ValueBox;

public class BigDecimalBox extends ValueBox<BigDecimal> {
	public BigDecimalBox() {
		super(Document.get().createTextInputElement(), BigDecimalRenderer.INSTANCE, BigDecimalParser.INSTANCE);
	}
}
