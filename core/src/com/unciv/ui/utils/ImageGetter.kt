package com.unciv.ui.utils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.unciv.models.gamebasics.GameBasics
import com.unciv.models.gamebasics.tile.ResourceType

object ImageGetter {
    private const val whiteDotLocation = "OtherIcons/whiteDot.png"

    // When we used to load images directly from different files, without using a texture atlas,
    // The draw() phase of the main screen would take a really long time because the BatchRenderer would
    // always have to switch between like 170 different textures.
    // So, we now use TexturePacker in the DesktopLauncher class to pack all the different images into single images,
    // and the atlas is what tells us what was packed where.
    var atlas = TextureAtlas("game.atlas")
    fun getWhiteDot() =  getImage(whiteDotLocation)
    fun getDot(dotColor: Color) = getWhiteDot().apply { color = dotColor}

    fun getExternalImage(fileName:String): Image {
        return Image(TextureRegion(Texture("ExtraImages/$fileName.png")))
    }

    fun getImage(fileName: String): Image {
        return Image(getTextureRegion(fileName))
    }

    fun getDrawable(fileName: String): TextureRegionDrawable {
        val drawable = TextureRegionDrawable(getTextureRegion(fileName))
        drawable.minHeight = 0f
        drawable.minWidth = 0f
        return drawable
    }

    private fun getTextureRegion(fileName: String): TextureRegion {
        try {
            val region = atlas.findRegion(fileName.replace(".png",""))

            if(region==null)
                throw Exception("Could not find $fileName")
            return region
        } catch (ex: Exception) {
            return getTextureRegion(whiteDotLocation)
        }
    }

    fun imageExists(fileName:String): Boolean {
        return atlas.findRegion(fileName)!=null
    }

    fun techIconExists(techName:String): Boolean {
        return imageExists("TechIcons/$techName")
    }

    fun getStatIcon(statName: String): Image {
        return getImage("StatIcons/$statName")
                .apply { setSize(20f,20f)}
    }

    fun getUnitIcon(unitName:String,color:Color= Color.BLACK):Image{
        return getImage("UnitIcons/$unitName").apply { this.color=color }
    }

    val foodCircleColor =  colorFromRGB(129, 199, 132)
    val productionCircleColor = Color.BROWN.cpy().lerp(Color.WHITE,0.5f)!!
    val goldCircleColor = Color.GOLD.cpy().lerp(Color.WHITE,0.5f)!!
    fun getImprovementIcon(improvementName:String, size:Float=20f):Actor{
        val iconGroup = getImage("ImprovementIcons/$improvementName").surroundWithCircle(size)


        val improvement = GameBasics.TileImprovements[improvementName]!!
        when {
            improvement.food>0 -> iconGroup.circle.color= foodCircleColor
            improvement.production>0 -> iconGroup.circle.color= productionCircleColor
            improvement.gold>0 -> iconGroup.circle.color= goldCircleColor
            improvement.science>0 -> iconGroup.circle.color= Color.BLUE.cpy().lerp(Color.WHITE,0.5f)
            improvement.culture>0 -> iconGroup.circle.color= Color.PURPLE.cpy().lerp(Color.WHITE,0.5f)
        }

        return iconGroup
    }

    fun getConstructionImage(construction: String): Image {
        if(GameBasics.Buildings.containsKey(construction)) return getImage("BuildingIcons/$construction")
        if(GameBasics.Units.containsKey(construction)) return getUnitIcon(construction)
        if(construction=="Nothing") return getImage("OtherIcons/Stop")
        return getStatIcon(construction)
    }

    fun getPromotionIcon(promotionName:String):Image{
        return getImage("UnitPromotionIcons/" + promotionName.replace(' ', '_') + "_(Civ5)")
    }

    fun getBlue() = Color(0x004085bf)

    fun getCircle() = getImage("OtherIcons/Circle")

    fun getBackground(color:Color): Drawable {
        return getDrawable(whiteDotLocation).tint(color)
    }

    fun refreshAltas() {
        atlas.dispose() // To avoid OutOfMemory exceptions
        atlas = TextureAtlas("game.atlas")
    }

    fun getResourceImage(resourceName: String, size:Float): Actor {
        val iconGroup = getImage("ResourceIcons/$resourceName").surroundWithCircle(size)
        val resource = GameBasics.TileResources[resourceName]!!
        when {
            resource.food>0 -> iconGroup.circle.color= foodCircleColor
            resource.production>0 -> iconGroup.circle.color= productionCircleColor
            resource.gold>0 -> iconGroup.circle.color= goldCircleColor
        }

        if(resource.resourceType==ResourceType.Luxury){
            val happiness = getStatIcon("Happiness")
            happiness.setSize(size/2,size/2)
            happiness.x = iconGroup.width-happiness.width
            iconGroup.addActor(happiness)
        }
        if(resource.resourceType==ResourceType.Strategic){
            val production = getStatIcon("Production")
            production.setSize(size/2,size/2)
            production.x = iconGroup.width-production.width
            iconGroup.addActor(production)
        }
        return iconGroup
    }

    fun getTechIconGroup(techName: String): Group {
        return getImage("TechIcons/$techName").surroundWithCircle(60f)
    }

    fun getProgressBarVertical(width:Float,height:Float,percentComplete:Float,progressColor:Color,backgroundColor:Color): Table {
        val advancementGroup = Table()
        val completionHeight = height * percentComplete
        advancementGroup.add(getImage(whiteDotLocation).apply { color = backgroundColor }).width(width).height(height-completionHeight).row()
        advancementGroup.add(getImage(whiteDotLocation).apply { color= progressColor}).width(width).height(completionHeight)
        advancementGroup.pack()
        return advancementGroup
    }

    fun getHealthBar(currentHealth: Float, maxHealth: Float, healthBarSize: Float): Table {
        val healthPercent = currentHealth / maxHealth
        val healthBar = Table()
        val healthPartOfBar = getWhiteDot()
        healthPartOfBar.color = when {
            healthPercent > 2 / 3f -> Color.GREEN
            healthPercent > 1 / 3f -> Color.ORANGE
            else -> Color.RED
        }
        val emptyPartOfBar = getDot(Color.BLACK)
        healthBar.add(healthPartOfBar).width(healthBarSize * healthPercent).height(5f)
        healthBar.add(emptyPartOfBar).width(healthBarSize * (1 - healthPercent)).height(5f)
        healthBar.pack()
        return healthBar
    }

    fun getLine(startX:Float,startY:Float,endX:Float,endY:Float, width:Float): Image {
        /** The simplest way to draw a line between 2 points seems to be:
         * A. Get a pixel dot, set its width to the required length (hypotenuse)
         * B. Set its rotational center, and set its rotation
         * C. Center it on the point where you want its center to be
         */

        // A
        val line = getWhiteDot()
        val deltaX = (startX-endX).toDouble()
        val deltaY = (startY-endY).toDouble()
        line.width = Math.sqrt(deltaX*deltaX+deltaY*deltaY).toFloat()
        line.height = width // the width of the line, is the height of the

        // B
        line.setOrigin(Align.center)
        val radiansToDegrees = 180 / Math.PI
        line.rotation = (Math.atan2(deltaY, deltaX) * radiansToDegrees).toFloat()

        // C
        line.x = (startX+endX)/2 - line.width/2
        line.y = (startY+endY)/2 - line.height/2

        return line
    }
}

