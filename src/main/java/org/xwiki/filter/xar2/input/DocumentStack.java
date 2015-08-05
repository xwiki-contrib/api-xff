package org.xwiki.filter.xar2.input;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rest.model.jaxb.Class;
import org.xwiki.rest.model.jaxb.Page;

public class DocumentStack {
	private DocumentReference reference;
	private Page xPage;
	private Class xClass;

	public Page getxPage() {
		return xPage;
	}

	public void setxPage(Page xPage) {
		this.xPage = xPage;
	}

	public Class getxClass() {
		return xClass;
	}

	public void setxClass(Class xClass) {
		this.xClass = xClass;
	}

	public DocumentReference getReference() {
		return reference;
	}

	public void setReference(DocumentReference reference) {
		this.reference = reference;
	}
}
