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

package org.luwrain.app.mail;

import java.util.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.io.json.Message;

public final class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{
	    new SimpleShortcutCommand("mail"),
	    new SimpleShortcutCommand("message"),
	};
    }

    @Override public ExtensionObject[] getExtObjects(Luwrain luwrain)
    {
	return new ExtensionObject[]{
	    new SimpleShortcut("mail", App.class),

	    new Shortcut() {
		@Override public String getExtObjName()
		{
		    return "message";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    NullCheck.notNullItems(args, "args");
		    if (args.length == 0)
			return new Application[]{new org.luwrain.app.message.App()};
		    final List<Application> res = new ArrayList();
		    for(String a: args)
		    {
			final Message m = Message.fromString(a);
			if (m != null)
			    res.add(new org.luwrain.app.message.App(m));
		    }
		    if (res.isEmpty())
			return new Application[]{new org.luwrain.app.message.App()};
		    return res.toArray(new Application[res.size()]);

		}
	    }
	};
    }
}
