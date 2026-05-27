package ua.notion.presentation.game.assets;

/**
 * Sprite-region constants for decorative map objects.
 *
 * <p>Each constant group defines the source rectangle (x, y, width, height) inside the
 * corresponding spritesheet PNG, plus convenient factory helpers used by the map generator.
 *
 * <h3>Spritesheets referenced</h3>
 *
 * <ul>
 *   <li>{@code items&objects/summer and spring items.png} — 192×272 px
 *   <li>{@code garden/misc.png} — 323×310 px
 *   <li>{@code Objects/Fence's copiar.png} — 48×80 px
 *   <li>{@code Objects/chest.png} — 32×32 px
 * </ul>
 */
public final class ObjectSpriteAtlas {

  private ObjectSpriteAtlas() {
    throw new AssertionError("Cannot instantiate constants class");
  }

  // ── Spritesheet paths (relative to /assets/) ─────────────────────────

  public static final String ITEMS_SHEET = "items&objects/summer and spring items.png";
  public static final String MISC_SHEET = "garden/misc.png";
  public static final String FENCE_SHEET = "Objects/Fence's copiar.png";
  public static final String CHEST_SHEET = "Objects/chest.png";
  public static final String BUILDINGS_SHEET = "Objects/House.png";

  // ── Buildings (Objects/House.png) ───────────────────────────────────
  // Right-hand house sprite (front-facing facade with door and windows).
  // Approximate region tuned for the current asset: x=128, y=32, w=80, h=80.

  public static final int FARMHOUSE_SRC_X = 128;
  public static final int FARMHOUSE_SRC_Y = 32;
  public static final int FARMHOUSE_SRC_W = 80;
  public static final int FARMHOUSE_SRC_H = 80;

  public static final int SHOP_SRC_X = 160;
  public static final int SHOP_SRC_Y = 48;
  public static final int SHOP_SRC_W = 64;
  public static final int SHOP_SRC_H = 64;

  // ── House (items&objects/summer and spring items.png) ─────────────────
  // The full assembled house occupies roughly cols 0-5, rows 0-4 in 16px grid
  // Actual pixel region: x=0, y=0, w=96, h=80

  public static final int HOUSE_SRC_X = 0;
  public static final int HOUSE_SRC_Y = 0;
  public static final int HOUSE_SRC_W = 96;
  public static final int HOUSE_SRC_H = 80;

  // ── Door (items&objects/summer and spring items.png) ──────────────────
  // Door is at approximately x=96, y=48, w=16, h=32
  public static final int DOOR_SRC_X = 112;
  public static final int DOOR_SRC_Y = 48;
  public static final int DOOR_SRC_W = 16;
  public static final int DOOR_SRC_H = 32;

  // ── Trees (items&objects/summer and spring items.png) ─────────────────
  // Green tree: x=128, y=32, w=32, h=48
  public static final int GREEN_TREE_SRC_X = 128;
  public static final int GREEN_TREE_SRC_Y = 32;
  public static final int GREEN_TREE_SRC_W = 32;
  public static final int GREEN_TREE_SRC_H = 48;

  // Red/autumn tree: x=160, y=32, w=32, h=48
  public static final int RED_TREE_SRC_X = 160;
  public static final int RED_TREE_SRC_Y = 32;
  public static final int RED_TREE_SRC_W = 32;
  public static final int RED_TREE_SRC_H = 48;

  // ── Stones (items&objects/summer and spring items.png) ────────────────
  // Small stone pile: x=112, y=16, w=16, h=16
  public static final int STONE_SMALL_SRC_X = 112;
  public static final int STONE_SMALL_SRC_Y = 16;
  public static final int STONE_SMALL_SRC_W = 16;
  public static final int STONE_SMALL_SRC_H = 16;

  // Large stone pile: x=128, y=16, w=32, h=16
  public static final int STONE_LARGE_SRC_X = 128;
  public static final int STONE_LARGE_SRC_Y = 16;
  public static final int STONE_LARGE_SRC_W = 32;
  public static final int STONE_LARGE_SRC_H = 16;

  // ── Flowers (garden/misc.png) ────────────────────────────────────────
  // The misc.png has various flowers in the top-right area.
  // Row of flowers starts around y=16, spaced every 16px from x=160

  // Red rose: x=176, y=0, w=16, h=16
  public static final int FLOWER_RED_SRC_X = 176;
  public static final int FLOWER_RED_SRC_Y = 0;
  public static final int FLOWER_RED_SRC_W = 16;
  public static final int FLOWER_RED_SRC_H = 16;

  // Pink flower: x=192, y=0, w=16, h=16
  public static final int FLOWER_PINK_SRC_X = 192;
  public static final int FLOWER_PINK_SRC_Y = 0;
  public static final int FLOWER_PINK_SRC_W = 16;
  public static final int FLOWER_PINK_SRC_H = 16;

  // Yellow flower: x=208, y=0, w=16, h=16
  public static final int FLOWER_YELLOW_SRC_X = 208;
  public static final int FLOWER_YELLOW_SRC_Y = 0;
  public static final int FLOWER_YELLOW_SRC_W = 16;
  public static final int FLOWER_YELLOW_SRC_H = 16;

  // Blue flower: x=224, y=0, w=16, h=16
  public static final int FLOWER_BLUE_SRC_X = 224;
  public static final int FLOWER_BLUE_SRC_Y = 0;
  public static final int FLOWER_BLUE_SRC_W = 16;
  public static final int FLOWER_BLUE_SRC_H = 16;

  // Purple flower: x=240, y=0, w=16, h=16
  public static final int FLOWER_PURPLE_SRC_X = 240;
  public static final int FLOWER_PURPLE_SRC_Y = 0;
  public static final int FLOWER_PURPLE_SRC_W = 16;
  public static final int FLOWER_PURPLE_SRC_H = 16;

  // Sunflower: x=256, y=0, w=16, h=16
  public static final int FLOWER_SUNFLOWER_SRC_X = 256;
  public static final int FLOWER_SUNFLOWER_SRC_Y = 0;
  public static final int FLOWER_SUNFLOWER_SRC_W = 16;
  public static final int FLOWER_SUNFLOWER_SRC_H = 16;

  // ── Bushes (garden/misc.png) ─────────────────────────────────────────
  // Green bush cluster: x=160, y=128, w=32, h=32
  public static final int BUSH_SRC_X = 160;
  public static final int BUSH_SRC_Y = 128;
  public static final int BUSH_SRC_W = 32;
  public static final int BUSH_SRC_H = 32;

  // Small bush: x=208, y=144, w=16, h=16
  public static final int BUSH_SMALL_SRC_X = 208;
  public static final int BUSH_SMALL_SRC_Y = 144;
  public static final int BUSH_SMALL_SRC_W = 16;
  public static final int BUSH_SMALL_SRC_H = 16;

  // ── Big oak tree (garden/misc.png) ───────────────────────────────────
  // The large tree: x=64, y=192, w=80, h=96
  public static final int OAK_TREE_SRC_X = 64;
  public static final int OAK_TREE_SRC_Y = 192;
  public static final int OAK_TREE_SRC_W = 80;
  public static final int OAK_TREE_SRC_H = 96;

  // ── Bench (garden/misc.png) ──────────────────────────────────────────
  // Wooden bench: x=48, y=112, w=32, h=16
  public static final int BENCH_SRC_X = 48;
  public static final int BENCH_SRC_Y = 112;
  public static final int BENCH_SRC_W = 32;
  public static final int BENCH_SRC_H = 16;

  // ── Wooden fence (garden/misc.png) ───────────────────────────────────
  // Fence post vertical: x=0, y=0, w=16, h=32
  public static final int FENCE_WOOD_SRC_X = 0;
  public static final int FENCE_WOOD_SRC_Y = 0;
  public static final int FENCE_WOOD_SRC_W = 16;
  public static final int FENCE_WOOD_SRC_H = 32;

  // ── Mushroom (garden/misc.png) ───────────────────────────────────────
  // Red mushroom: x=192, y=176, w=16, h=16
  public static final int MUSHROOM_SRC_X = 192;
  public static final int MUSHROOM_SRC_Y = 176;
  public static final int MUSHROOM_SRC_W = 16;
  public static final int MUSHROOM_SRC_H = 16;

  // ── Stump (garden/misc.png) ──────────────────────────────────────────
  // Tree stump: x=288, y=256, w=32, h=32
  public static final int STUMP_SRC_X = 288;
  public static final int STUMP_SRC_Y = 256;
  public static final int STUMP_SRC_W = 32;
  public static final int STUMP_SRC_H = 32;

  // ── Chest (items sheet — closed brown, transparent bg) ───────────────
  public static final int CHEST_SRC_X = 160;
  public static final int CHEST_SRC_Y = 256;
  public static final int CHEST_SRC_W = 16;
  public static final int CHEST_SRC_H = 16;

  // ── Stone path (garden/misc.png) ─────────────────────────────────────
  // Paving stones: x=48, y=80, w=16, h=16
  public static final int STONE_PATH_SRC_X = 48;
  public static final int STONE_PATH_SRC_Y = 80;
  public static final int STONE_PATH_SRC_W = 16;
  public static final int STONE_PATH_SRC_H = 16;

  // Small stepping stones: x=64, y=80, w=16, h=16
  public static final int STEPPING_STONE_SRC_X = 64;
  public static final int STEPPING_STONE_SRC_Y = 80;
  public static final int STEPPING_STONE_SRC_W = 16;
  public static final int STEPPING_STONE_SRC_H = 16;

  // ── Watering can / bucket (items&objects/summer and spring items.png) ─
  // Bucket: x=48, y=160, w=16, h=16
  public static final int BUCKET_SRC_X = 48;
  public static final int BUCKET_SRC_Y = 160;
  public static final int BUCKET_SRC_W = 16;
  public static final int BUCKET_SRC_H = 16;

  // ── Picnic blanket (items&objects/summer and spring items.png) ────────
  // Red/white blanket: x=0, y=192, w=48, h=48
  public static final int BLANKET_SRC_X = 0;
  public static final int BLANKET_SRC_Y = 192;
  public static final int BLANKET_SRC_W = 48;
  public static final int BLANKET_SRC_H = 48;
}
