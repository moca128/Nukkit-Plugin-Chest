package moca.plugin.psmplugin;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;

import java.io.File;
import java.util.HashMap;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
//りんごんサーバーはtedoさんのeconomyを使っています。tedoさんのAPIじゃないなら下のimport文を変えてお使いください。
//もし使えないときは//のところを見て対応してください。
import tedo.EconomySystemAPI.EconomySystemAPI;
public class Main extends PluginBase implements Listener{
	public Config data;
	public HashMap<String, HashMap<String, HashMap<String, String>>> sign = new HashMap<String, HashMap<String, HashMap<String, String>>>();
	public HashMap<String, String> pos = new HashMap<String, String>();
	//[1]EconomySystemAPIという名前じゃないときはEconomySyst5emAPIを消して自分の使っているeconomyの名前を入れてね。ここに引っかかった人は[2]へ
	public EconomySystemAPI economy;
	@SuppressWarnings("unchecked")
	public void onEnable() {
		getLogger().info("これはもかのプラグインです。");
		this.getServer().getPluginManager().registerEvents(this, this);
		//[2][1]と同じ様に変えてね。
		this.economy = (EconomySystemAPI) getServer().getPluginManager().getPlugin("EconomySystemAPI");
		this.getDataFolder().mkdirs();
		this.data = new Config(new File(this.getDataFolder(), "data.yml"),Config.YAML);
		this.data.getAll().forEach((level, data) -> this.sign.put(level, (HashMap<String, HashMap<String, String>>) data));
		if (!(this.sign.containsKey(getServer().getDefaultLevel().getName()))) {
			HashMap<String, HashMap<String, String>> data = new HashMap<String, HashMap<String, String>>();
			this.sign.put(getServer().getDefaultLevel().getName(), data);
		}
	}
	public void onDisable() {
		this.sign.forEach((level, data) -> this.data.set(level, data));
		this.data.save();
	}
	@EventHandler
	public void onlevelLoad(LevelLoadEvent event) {
		Level level = event.getLevel();
		String name = level.getName();
		if (!(this.sign.containsKey(name))) {
			HashMap<String, HashMap<String, String>> data = new HashMap<String, HashMap<String, String>>();
			this.sign.put(name, data);
		}
	}
	@EventHandler
	public void onsign(SignChangeEvent event) {
		Player p = event.getPlayer();
		Block b = event.getBlock();
		String n = p.getName().toLowerCase();
		String[] text = event.getLines();
		String x = String.valueOf((int) b.x);
		String y = String.valueOf((int) b.y);
		String z = String.valueOf((int) b.z);
		String level = p.getLevel().getName();
		if(text[0].equals("PSM")) {
		try {
			if (text[0] != null && text[1] != null && text[2] != null && text[3] != null) {
					if (isInt(text[1])) {
						if (isInt(text[3])) {
							String money = text[1];
							Item item = Item.fromString(text[2]);
							String id = String.valueOf(item.getId());
							String damage = String.valueOf(item.getDamage());
							String amount = text[3];
							if(Long.parseLong(money)>=500) {
								event.setLine(0, "§b"+n);
								event.setLine(1, "§e値段 : " + money + "money");
								event.setLine(2, "§aアイテム : " + item.getName());
								event.setLine(3, "§a個数 : " + amount + "個");
								HashMap<String, String> data = new HashMap<String, String>();
								String pos = x + ":" + y + ":" + z;
								data.put("data", "PSM");
								data.put("money", money);
								data.put("id", id);
								data.put("damage", damage);
								data.put("amount", amount);
								data.put("player", p.getName());
								HashMap<String, HashMap<String, String>> d = (HashMap<String, HashMap<String, String>>) this.sign.get(level);
								d.put(pos, data);
								this.sign.put(level, d);
								p.sendMessage("§bPSM看板をつくりました");
							}else {
								p.sendMessage("§bPSMは500moneyから売ることができます");
							}
						}else {
							p.sendMessage("§b4行目にいくつ売るか決めます");
						}
					}else {
						p.sendMessage("§b2行目には金額を入力します");
					}
				}
			}
			catch(ArrayIndexOutOfBoundsException e){
			}
		}
	}
	private boolean isInt(String number) {
		try {
			Double.parseDouble(number);
			return true;
		}
		catch(NumberFormatException ex){
			return false;
		}
	}
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getBlock();
		if (block.getId() == 63 || block.getId() == 68) {
			double c = block.x;
			double h = block.y-1;
			double e = block.z;
			Level l = block.getLevel();
			String x = String.valueOf((int) block.x);
			String y = String.valueOf((int) block.y);
			String z = String.valueOf((int) block.z);
			String level = block.getLevel().getName();
			String pos = x + ":" + y + ":" + z;
			BlockEntity chest = l.getBlockEntity(new Vector3(c, h, e));
			if (this.sign.containsKey(level)) {
				HashMap<String, HashMap<String, String>> data = this.sign.get(level);
				if (data.containsKey(pos)) {
					HashMap<String, String> d = (HashMap<String, String>) data.get(pos);
					Player player = event.getPlayer();
					String name = player.getName().toLowerCase();
					event.setCancelled();
					if (this.pos.containsKey(name) && this.pos.get(name).equals(pos)) {
						long money;
						if (d.get("data").equals("PSM")) {
							if (chest instanceof BlockEntityChest) {
								money = Long.parseLong((String) d.get("money"));
								if (money <= this.economy.getMoney(name)) {
									int id = Integer.parseInt((String) d.get("id"));
									int damage = Integer.parseInt((String) d.get("damage"));
									int amount = Integer.parseInt((String) d.get("amount"));
									String p = d.get("player");
									Item item = Item.get(id, damage, amount);
									if (player.getInventory().canAddItem(item)) {
										if(((BlockEntityChest) chest).getInventory().contains(item)) {
											this.economy.addMoney(p, money);
											this.economy.delMoney(name, money);
											this.pos.remove(name);
											player.getInventory().addItem(item);
											((BlockEntityChest) chest).getInventory().removeItem(new Item(id,damage,amount));
											player.sendMessage("§b購入しました");
										}else {
											player.sendMessage("§bチェストにアイテムが入っていません");
										}
									}else {
										player.sendMessage("§bあなたのインベントリの空きがありません");
									}
								}else {
									player.sendMessage("§bあなたのmoneyが足りません");
								}
							}else {
								player.sendMessage("§b看板の下にチェストがありません。");
							}
						}
					}else{
						if(d.get("data").equals("PSM")) {
								this.pos.put(name, pos);
								player.sendPopup("§b購入する場合はもう一度タップしてください");
						}
					}
				}
			}
		}
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block.getId() == 63 || block.getId() == 68) {
			String x = String.valueOf((int) block.x);
			String y = String.valueOf((int) block.y);
			String z = String.valueOf((int) block.z);
			String level = block.getLevel().getName();
			String pos = x + ":" + y + ":" + z;
			if (this.sign.containsKey(level)) {
				HashMap<String, HashMap<String, String>> data = this.sign.get(level);
				if (data.containsKey(pos)) {
					Player player = event.getPlayer();
					if (player.isOp()) {
						data.remove(pos);
						this.sign.put(level, data);
						player.sendMessage("§bPSMの看板を破壊しました");
					}else{
						event.setCancelled();
						player.sendMessage("§bこの看板はOPじゃないと破壊できません。");
					}
				}
			}
		}
	}
}