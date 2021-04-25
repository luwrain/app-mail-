/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.message;

import java.io.*;
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.mail.MailMessage;
import org.luwrain.io.json.*;
import org.luwrain.app.base.*;
import org.luwrain .util.*;

final class MainLayout extends LayoutBase
{
    private final App app;
    private final MessageArea messageArea;

    MainLayout(App app)
    {
	super(app);
	this.app = app;
	final Settings.PersonalInfo sett = Settings.createPersonalInfo(app.getLuwrain().getRegistry());
	final List<String> text = new ArrayList();
	text.addAll(Arrays.asList(app.messageContent.getTextAsArray()));
	text.add("");
	text.addAll(Arrays.asList(TextUtils.splitLinesAnySeparator(sett.getSignature(""))));
	final MessageArea.Params params = new MessageArea.Params();
	params.context = getControlContext();
	params.name = app.getStrings().appName();
	params.text = text.toArray(new String[text.size()]);
	this.messageArea = new MessageArea(params){
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case OK:
			    return actSend();
			}
		    return super.onSystemEvent(event);
		}
	    };
	setAreaLayout(messageArea, actions(
					   action("sent", app.getStrings().actionSend(), MainLayout.this::actSend),
					   action("attach", app.getStrings().actionAttachFile(), new InputEvent(InputEvent.Special.INSERT), this::actAttachFile),
					   					   action("delete-attachment", app.getStrings().actionDeleteAttachment(), this::actDeleteAttachment)
					   ));
    }

    private boolean actSend()
    {
	if (app.send(getMailMessage(), true))
	{
	    app.getLuwrain().runWorker(org.luwrain.pim.workers.Smtp.NAME);
	    app.closeApp();
	}
	return true;
    }

    private boolean actEditTo()
    {
	final String res = app.getConv().editTo();
	if (res != null)
	    messageArea.setTo(res);
	return true;
    }

    private boolean actEditCc(MessageArea area)
    {
	NullCheck.notNull(area, "area");
	final String res = app.getConv().editCc(area.getCc());
	if (res != null)
	    area.setCc(res);
	return true;
    }

    private boolean actAttachFile()
    {
	final File file = app.getConv().attachment();
	if (file == null)
	    return true;
	messageArea.addAttachment(file.getAbsoluteFile());
	return true;
    }

    private boolean actDeleteAttachment()
    {
	final int index = messageArea.getHotPointY();
	final MessageArea.Attachment a = messageArea.getAttachmentByLineIndex(index);
	if (a == null)
	    return false;
	if (!app.getConv().confirmAttachmentDeleting(a.getFile()))
	    return true;
	messageArea.removeAttachmentByLineIndex(index);
	app.getLuwrain().message("Прикрепление " + a.getName() + " исключено из сообщения", Luwrain.MessageType.OK);
	return true;
    }

    private boolean isReadyForSending(MessageArea area)
    {
	NullCheck.notNull(area, "area");
	if (area.getTo().trim().isEmpty())
	{
	    app.message("Не указан получатель сообщения", Luwrain.MessageType.ERROR);//FIXME:
	    area.focusTo();
	    return false;
	}
	if (area.getSubject().trim().isEmpty())
	{
	    app.message("Не указана тема сообщения", Luwrain.MessageType.ERROR);
	    area.focusSubject();
	    return false;
	}
	return true;
    }

    private MailMessage getMailMessage()
    {
	final MailMessage msg = new MailMessage();
	msg.setTo(App.splitAddrs(messageArea.getTo()));
	msg.setCc(App.splitAddrs(messageArea.getCc()));
	msg.setSubject(messageArea.getSubject());
	msg.setText(messageArea.getText());
	final List<String> a = new ArrayList();
		   for(File f: messageArea.getAttachmentFiles())
		       a.add(f.getAbsolutePath());
		   msg.setAttachments(a.toArray(new String[a.size()]));
	return msg;
    }
}
