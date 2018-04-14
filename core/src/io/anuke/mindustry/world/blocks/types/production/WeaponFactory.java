package io.anuke.mindustry.world.blocks.types.production;

import com.badlogic.gdx.graphics.Color;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.net.Net;
import io.anuke.mindustry.net.NetEvents;
import io.anuke.mindustry.resource.ItemStack;
import io.anuke.mindustry.resource.Upgrade;
import io.anuke.mindustry.content.UpgradeRecipes;
import io.anuke.mindustry.resource.Weapon;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.function.Listenable;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.scene.style.TextureRegionDrawable;
import io.anuke.ucore.scene.ui.ImageButton;
import io.anuke.ucore.scene.ui.Tooltip;
import io.anuke.ucore.scene.ui.layout.Table;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.*;

public class WeaponFactory extends Block{

    public WeaponFactory(String name){
        super(name);
        solid = true;
        destructible = true;
    }

    @Override
    public void draw(Tile tile) {
        super.draw(tile);

        for(int i : Mathf.signs){
            Draw.color(Color.YELLOW);
            Draw.alpha(0.1f + Mathf.absin(Timers.time(), 12f, 0.2f));
            Draw.rect("white", tile.drawx() + i*3.5f, tile.drawy(), 1, 8);
            Draw.color(Color.ORANGE);
            Draw.alpha(0.5f);
            Draw.rect("white", tile.drawx() + i*3.5f, tile.drawy() + Mathf.sin(Timers.time() + i *77, 13f, 4f), 1, 2);
        }

        Draw.reset();
    }

    @Override
    public boolean isConfigurable(Tile tile){
        return !Vars.android;
    }

    @Override
    public void buildTable(Tile tile, Table table) {
        int i = 0;

        Table content = new Table();

        for(Upgrade upgrade : Upgrade.getAllUpgrades()){
            if(!(upgrade instanceof Weapon)) continue;
            Weapon weapon = (Weapon)upgrade;

            ItemStack[] requirements = UpgradeRecipes.get(weapon);

            Table tiptable = new Table();

            Listenable run = ()->{
                tiptable.clearChildren();

                String description = weapon.description;

                tiptable.background("pane");
                tiptable.add("[orange]" + weapon.localized(), 0.5f).left().padBottom(2f);

                Table reqtable = new Table();

                tiptable.row();
                tiptable.add(reqtable).left();

                if(!control.upgrades().hasWeapon(weapon)){
                    for(ItemStack s : requirements){

                        int amount = Math.min(state.inventory.getAmount(s.item), s.amount);
                        reqtable.addImage(s.item.region).padRight(3).size(8*2);
                        reqtable.add(
                                (amount >= s.amount ? "" : "[RED]")
                                        + amount + " / " +s.amount, 0.5f).left();
                        reqtable.row();
                    }
                }

                tiptable.row();
                tiptable.add().size(4);
                tiptable.row();
                tiptable.add("[gray]" + description).left();
                tiptable.row();
                if(control.upgrades().hasWeapon(weapon)){
                    tiptable.add("$text.purchased").padTop(4).left();
                }
                tiptable.margin(8f);
            };

            run.listen();

            Tooltip<Table> tip = new Tooltip<>(tiptable, run);

            tip.setInstant(true);

            ImageButton button = content.addImageButton("white", 8*4, () -> {

                if(Net.client()){
                    NetEvents.handleUpgrade(weapon);
                }else{
                    state.inventory.removeItems(requirements);
                    control.upgrades().addWeapon(weapon);
                    run.listen();
                    Effects.sound("purchase");
                }
            }).size(49f, 54f).padBottom(-5).get();

            button.setDisabled(() -> control.upgrades().hasWeapon(weapon) || !state.inventory.hasItems(requirements));
            button.getStyle().imageUp = new TextureRegionDrawable(Draw.region(weapon.name));
            button.addListener(tip);

            if(++i % 3 == 0){
                content.row();
            }
        }

        table.add(content).padTop(140f);
    }
}
