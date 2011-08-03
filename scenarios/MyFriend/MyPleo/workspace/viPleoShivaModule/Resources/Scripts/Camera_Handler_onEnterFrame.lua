--------------------------------------------------------------------------------
--  Handler.......... : onEnterFrame
--  Author........... : Tiago Paiva
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function Camera.onEnterFrame (  )
--------------------------------------------------------------------------------
	
    local hObject = this.getObject ( )
    local hDynObj = this.hBody ( )
    this.nAngleX ( math.clamp ( this.nAngleX ( ) + this.nDeltaY ( ) / 12, -60, 60 ) )
    this.nAngleY ( this.nAngleY ( ) - this.nDeltaX ( ) / 5 )
    
    --Camera rotation Joypad
    --[[
        object.matchTranslation ( hObject, hDynObj, object.kGlobalSpace )
        object.translate ( hObject, 0, 1.2, 0, object.kGlobalSpace )
        object.resetRotation ( hObject, object.kGlobalSpace )
        object.setRotation ( hObject, 0, this.nAngleY ( ), 0, object.kGlobalSpace )
        object.rotate ( hObject, this.nAngleX ( ), 0, 0, object.kLocalSpace )
    --]]
    
    --Automatic Camera v1
    --[[
        local x, y, z = object.getTranslation ( hDynObj, object.kGlobalSpace )
        local _x, height, _z = object.getTranslation ( hObject, object.kGlobalSpace )
        object.lookAt ( hObject, x, y, z, object.kGlobalSpace, 1 )
        local _rx, ry, _rz = object.getRotation ( hObject, object.kGlobalSpace )
        object.matchTranslation ( hObject, hDynObj, object.kGlobalSpace )
        object.setRotation ( hObject, 0, ry, 0, object.kGlobalSpace )
        object.rotate ( hObject, this.nCameraRotationX ( ), 0, 0, object.kLocalSpace )
        local dx, dy, dz = object.getDirection ( hObject, object.kGlobalSpace )
        local dist = 5
        local hHitObject, nHitDist, nHitSurfaceID = scene.getFirstHitCollider ( application.getCurrentUserScene ( ), x, y, z, -dx, -dy, -dz, 5 )
        if ( hHitObject )
        then
            dist = math.min ( nHitDist - 0.5, dist )
        end
        dx, dy, dz = math.vectorSetLength ( -dx, -dy, -dz, dist )
        object.translate ( hObject, dx, dy, dz, object.kGlobalSpace )
    --]]
    
    --Automatic Camera v2
    -- [[
        local distance = 55--8
        local height = 35 --5
        
        if ( this.bZoom ( ) )
        then
            distance = 4
            height = 2.5
        end
        
        local x, y, z = object.getTranslation ( hDynObj, object.kGlobalSpace )
        local x2, y2, z2 = object.getTranslation ( hObject, object.kGlobalSpace )
        local tx, ty, tz = math.vectorSubtract ( x2, 0, z2, x, 0, z )
        local dist = math.vectorLength ( tx, ty, tz )
        distance = math.max ( math.min ( distance, dist ), 4 )
        tx, ty, tz = math.vectorSetLength ( tx, ty, tz, distance )
        tx, ty, tz = math.vectorAdd ( x, y, z, tx, ty, tz )
        --object.setTranslation ( hObject, tx, ty + height, tz, object.kGlobalSpace )
        object.translateTo ( hObject, tx, ty + height, tz, object.kGlobalSpace, 0.1 )
        object.lookAt ( hObject, x, y, z, object.kGlobalSpace, 1 )
    --]]
    
    --Test Camera rotation + body move Joypad
    --[[
        local x, y, z = object.getTranslation ( hDynObj, object.kGlobalSpace )
        local fx, fy, fz = object.getDirection ( hObject, object.kGlobalSpace )
        fx, fy, fz = math.vectorSetLength ( fx, fy, fz, this.nMoveY ( ) * 20 )
        dynamics.addForce ( hDynObj, fx, fy, fz, object.kGlobalSpace )
        object.matchTranslation ( hObject, hDynObj, object.kGlobalSpace )
        object.translate ( hObject, 0, 1.2, 0, object.kGlobalSpace )
        object.setRotation ( hObject, 0, this.nAngleY ( ), 0, object.kGlobalSpace )
        object.matchRotation ( hDynObj, hObject, object.kGlobalSpace )
    --]]
    
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
