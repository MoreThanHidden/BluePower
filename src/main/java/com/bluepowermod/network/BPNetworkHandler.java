/*
 * This file is part of Blue Power.
 *
 *     Blue Power is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Blue Power is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Blue Power.  If not, see <http://www.gnu.org/licenses/>
 */

package com.bluepowermod.network;

import com.bluepowermod.network.message.*;
import com.bluepowermod.reference.Refs;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.qmunity.lib.network.NetworkHandler;


public class BPNetworkHandler {

    public static final NetworkHandler INSTANCE = new NetworkHandler(Refs.MODID);

    public static void initBP() {

        INSTANCE.registerPacket(MessageGuiUpdate.class, MessageGuiUpdate.class, Side.SERVER);
        INSTANCE.registerPacket(MessageUpdateTextfield.class, MessageUpdateTextfield.class, Side.SERVER);
        INSTANCE.registerPacket(MessageDebugBlock.class, MessageDebugBlock.class, Side.CLIENT);
        INSTANCE.registerPacket(MessageSendClientServerTemplates.class, MessageSendClientServerTemplates.class, Side.CLIENT);
        INSTANCE.registerPacket(MessageRedirectTubeStack.class, MessageRedirectTubeStack.class, Side.CLIENT);
        INSTANCE.registerPacket(MessageServerTickTime.class, Side.CLIENT);

        INSTANCE.registerPacket(MessageSyncMachineBacklog.class, MessageSyncMachineBacklog.class, Side.CLIENT);
    }

}
