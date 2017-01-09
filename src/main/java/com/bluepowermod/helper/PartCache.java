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

package com.bluepowermod.helper;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import uk.co.qmunity.lib.part.IQLPart;
import uk.co.qmunity.lib.part.MultipartCompat;

public class PartCache<CachedPart extends IQLPart> extends LocationCache<CachedPart> {

    public <T> PartCache(World world, BlockPos pos, Class<? extends IQLPart> searchedParts) {

        super(world, pos, searchedParts);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CachedPart getNewValue(World world, BlockPos pos, Object... extraArgs) {

        return (CachedPart) MultipartCompat.getPart(world, pos, (Class<? extends IQLPart>) extraArgs[0]);
    }

}
