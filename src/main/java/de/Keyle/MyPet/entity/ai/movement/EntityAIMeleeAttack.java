/*
 * This file is part of MyPet
 *
 * Copyright (C) 2011-2013 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.entity.ai.movement;

import de.Keyle.MyPet.entity.ai.EntityAIGoal;
import de.Keyle.MyPet.entity.types.EntityMyPet;
import net.minecraft.server.v1_5_R2.EntityLiving;
import org.bukkit.craftbukkit.v1_5_R2.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityAIMeleeAttack extends EntityAIGoal
{
    EntityMyPet petEntity;
    EntityLiving targetEntity;
    double range;
    float walkSpeed;
    private int ticksUntilNextHitLeft = 0;
    private int ticksUntilNextHit;
    private int timeUntilNextNavigationUpdate;

    public EntityAIMeleeAttack(EntityMyPet petEntity, float walkSpeed, double range, int ticksUntilNextHit)
    {
        this.petEntity = petEntity;
        this.walkSpeed = walkSpeed;
        this.range = range * range;
        this.ticksUntilNextHit = ticksUntilNextHit;
    }

    public boolean shouldStart()
    {
        EntityLiving targetEntity = this.petEntity.getGoalTarget();
        if (targetEntity == null)
        {
            return false;
        }
        if (!targetEntity.isAlive())
        {
            return false;
        }
        if (petEntity.getMyPet().getRangedDamage() > 0 && this.petEntity.e(targetEntity.locX, targetEntity.boundingBox.b, targetEntity.locZ) >= 16)
        {
            return false;
        }
        this.targetEntity = targetEntity;
        return this.petEntity.aD().canSee(targetEntity);
    }

    @Override
    public boolean shouldFinish()
    {
        if (this.petEntity.getGoalTarget() == null)
        {
            return false;
        }
        else if (this.targetEntity != this.petEntity.getGoalTarget())
        {
            return false;
        }
        else if (!this.targetEntity.isAlive())
        {
            return false;
        }
        if (petEntity.getMyPet().getRangedDamage() > 0 && this.petEntity.e(targetEntity.locX, targetEntity.boundingBox.b, targetEntity.locZ) >= 16)
        {
            return false;
        }
        return true;
    }

    @Override
    public void start()
    {
        this.petEntity.getNavigation().a(this.targetEntity, this.walkSpeed);
        this.timeUntilNextNavigationUpdate = 0;
    }

    @Override
    public void finish()
    {
        EntityTargetEvent.TargetReason reason = targetEntity.isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
        CraftEventFactory.callEntityTargetEvent(this.petEntity, null, reason);

        this.targetEntity = null;
        this.petEntity.getNavigation().g();
    }

    @Override
    public void schedule()
    {
        this.petEntity.getControllerLook().a(targetEntity, 30.0F, 30.0F);
        if (((this.petEntity.aD().canSee(targetEntity))) && (--this.timeUntilNextNavigationUpdate <= 0))
        {
            this.timeUntilNextNavigationUpdate = (4 + this.petEntity.aE().nextInt(7));
            this.petEntity.getNavigation().a(targetEntity, this.walkSpeed);
        }
        if ((this.petEntity.e(targetEntity.locX, targetEntity.boundingBox.b, targetEntity.locZ) <= this.range) && (this.ticksUntilNextHitLeft-- <= 0))
        {
            this.ticksUntilNextHitLeft = ticksUntilNextHit;
            if (this.petEntity.bG() != null)
            {
                this.petEntity.bK();
            }
            this.petEntity.m(targetEntity);
        }
    }
}