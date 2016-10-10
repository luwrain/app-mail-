
package org.luwrain.app.mail;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.*;

class RawMessageArea extends NavigationArea
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final MailApp app;
    private StoredMailMessage message;
    private String[] content = new String[0];

    RawMessageArea(Luwrain luwrain, MailApp app,
		Strings strings)
    {
	super(new DefaultControlEnvironment(luwrain));
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(app, "app");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.app = app;
	this.strings = strings;
    }

    boolean show(StoredMailMessage message)
    {
	if (message == null)
	{
	    this.message = null;
	    content = new String[0];
	luwrain.onAreaNewContent(this);
	    return true;
	}
	try {
	    final String str = new String(message.getRawMail(), "US-ASCII");
	    System.out.println("strlen " + str.length());
	content = str.split("\n");
	this.message = message;
	luwrain.onAreaNewContent(this);
	return true;
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    content = new String[0];
	    this.message = null;
	luwrain.onAreaNewContent(this);
	    return false;
	}
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isSpecial() && !event.isModified())
	    switch(event.getSpecial())
	    {
	    case F9:
app.launchMailFetch();
		return true;
	    case TAB:
app.gotoFolders();
	    return true;
	    case BACKSPACE:
app.gotoSummary();
	    return true;
	    case F5://FIXME:Action
		    return app.makeReply(null, false);
	    case F6://FIXME:Action
		return app.makeForward(null);
	    }
	if (event.isSpecial() && event.withShiftOnly())
	    switch(event.getSpecial())
	    {
	    case F5://FIXME:Action
		    return app.makeReply(null, true);
}
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch (event.getCode())
	{
	case CLOSE:
app.closeApp();
	    return true;
	case ACTION:
	    if (ActionEvent.isAction(event, "reply"))
	    {
app.makeReply(null, false);
		return true;
	    }
	    if (ActionEvent.isAction(event, "reply-all"))
	    {
app.makeReply(null, true);
		return true;
	    }
	    if (ActionEvent.isAction(event, "forward"))
	    {
app.makeForward(null);
		return true;
	    }
	    return false;


	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public Action[] getAreaActions()
    {
	return new Action[]{
	    new Action("reply", "Ответить"),
	    new Action("reply-all", "Ответить всем"),
	    new Action("forward", "Переслать"),
	};
    }

    @Override public String getAreaName()
    {
	return strings.messageAreaName();//FIXME:
    }

    @Override public int getLineCount()
    {
	return content.length > 1?content.length:1;
    }

    @Override public String getLine(int index)
    {
	return index < content.length?content[index]:"";
    }
}
