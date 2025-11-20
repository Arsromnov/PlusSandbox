import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main extends JPanel implements Runnable, MouseListener, MouseMotionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int CELL_SIZE = 4;
    private static final int COLS = WIDTH / CELL_SIZE;
    private static final int ROWS = HEIGHT / CELL_SIZE;
    
    private int[][] grid;
    private int[][] gridBuffer;
    private boolean running = true;
    private boolean paused = false;
    private boolean showSaveMenu = false;
    private boolean showLoadMenu = false;
    private Random random = new Random();
    
    // Элементы
    public static final int EMPTY = 0;
    public static final int SAND = 1;
    public static final int WATER = 2;
    public static final int LAVA = 3;
    public static final int FIRE = 4;
    public static final int EARTH = 5;
    public static final int STONE = 6;
    public static final int SMOKE = 7;
    public static final int SEED = 8;
    public static final int GRASS = 9;
    public static final int WOOD = 10;
    public static final int ICE = 11;
    public static final int OIL = 12;
    public static final int ERASER = 13;
    public static final int IRON = 14;
    public static final int NITROGEN = 15;
    public static final int UNBREAKABLE = 16;
    public static final int ACID = 17;
    public static final int GLASS = 18;
    public static final int DYNAMITE = 19;
    public static final int GOLD = 20;
    public static final int COPPER = 21;
    public static final int SALT = 22;
    public static final int CEMENT = 23;
    public static final int RUBBER = 24;
    public static final int GASOLINE = 25;
    public static final int MERCURY = 26;
    public static final int URANIUM = 27;
    public static final int MOLTEN_IRON = 28;
    public static final int MOLTEN_GOLD = 29;
    public static final int MOLTEN_COPPER = 30;
    public static final int LAMP = 31;
    
    private int currentElement = SAND;
    private int brushSize = 3;
    private boolean mousePressed = false;
    
    // Для FPS и скорости симуляции
    private int fps = 0;
    private int frameCount = 0;
    private long lastFpsTime = 0;
    private float simulationSpeed = 1.0f;
    
    // Для нагрева металлов
    private int[][] heatGrid;
    
    // Для жизни огня
    private int[][] fireLifeGrid;
    
    // Для формы кисти
    private boolean roundBrush = true; // true - круглая, false - квадратная
    
    // Для света ламп
    private int[][] lightGrid;
    
    // Для взрывов
    private List<Explosion> explosions = new ArrayList<>();
    
    // Для сохранения/загрузки
    private String saveFileName = "";
    private String[] saveFiles = new String[0];
    private int selectedSaveIndex = -1;
    
    class Explosion {
        int x, y, radius, life;
        
        Explosion(int x, int y, int radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.life = 20;
        }
        
        void update() {
            life--;
        }
        
        boolean isAlive() {
            return life > 0;
        }
    }
    
    public Main() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true);
        
        // Создаем папку saves если её нет
        File savesDir = new File("saves");
        if (!savesDir.exists()) {
            savesDir.mkdir();
        }
        
        grid = new int[COLS][ROWS];
        gridBuffer = new int[COLS][ROWS];
        heatGrid = new int[COLS][ROWS];
        fireLifeGrid = new int[COLS][ROWS];
        lightGrid = new int[COLS][ROWS];
        lastFpsTime = System.currentTimeMillis();
        refreshSaveFiles();
    }
    
    private void refreshSaveFiles() {
        saveFiles = getSaveFiles();
    }
    
    @Override
    public void run() {
        while (running) {
            long startTime = System.currentTimeMillis();
            
            if (!paused) {
                for (int i = 0; i < simulationSpeed; i++) {
                    updatePhysics();
                    updateLighting();
                }
                updateExplosions();
            }
            updateFPS();
            repaint();
            
            long endTime = System.currentTimeMillis();
            long sleepTime = Math.max(1, 16 - (endTime - startTime));
            
            try {
                Thread.sleep((long)(sleepTime / simulationSpeed));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void updateFPS() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsTime >= 1000) {
            fps = frameCount;
            frameCount = 0;
            lastFpsTime = currentTime;
        }
    }
    
    private void updatePhysics() {
        // Копируем текущее состояние в буфер
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                gridBuffer[x][y] = grid[x][y];
            }
        }
        
        // Обновляем физику снизу вверх для стабильности
        for (int y = ROWS - 2; y >= 0; y--) {
            for (int x = 0; x < COLS; x++) {
                int element = grid[x][y];
                
                if (element == EMPTY) continue;
                
                switch (element) {
                    case SAND: updateSand(x, y); break;
                    case WATER: updateWater(x, y); break;
                    case LAVA: updateLava(x, y); break;
                    case FIRE: updateFire(x, y); break;
                    case EARTH: updateEarth(x, y); break;
                    case STONE: updateStone(x, y); break;
                    case SMOKE: updateSmoke(x, y); break;
                    case SEED: updateSeed(x, y); break;
                    case GRASS: updateGrass(x, y); break;
                    case WOOD: updateWood(x, y); break;
                    case ICE: updateIce(x, y); break;
                    case OIL: updateOil(x, y); break;
                    case IRON: updateIron(x, y); break;
                    case NITROGEN: updateNitrogen(x, y); break;
                    case UNBREAKABLE: updateUnbreakable(x, y); break;
                    case ACID: updateAcid(x, y); break;
                    case GLASS: updateGlass(x, y); break;
                    case DYNAMITE: updateDynamite(x, y); break;
                    case GOLD: updateGold(x, y); break;
                    case COPPER: updateCopper(x, y); break;
                    case SALT: updateSalt(x, y); break;
                    case CEMENT: updateCement(x, y); break;
                    case RUBBER: updateRubber(x, y); break;
                    case GASOLINE: updateGasoline(x, y); break;
                    case MERCURY: updateMercury(x, y); break;
                    case URANIUM: updateUranium(x, y); break;
                    case MOLTEN_IRON: updateMoltenIron(x, y); break;
                    case MOLTEN_GOLD: updateMoltenGold(x, y); break;
                    case MOLTEN_COPPER: updateMoltenCopper(x, y); break;
                    case LAMP: updateLamp(x, y); break;
                }
            }
        }
        
        // Копируем буфер обратно в основную сетку
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                grid[x][y] = gridBuffer[x][y];
            }
        }
    }
    
    private void updateLighting() {
        // Сбрасываем свет
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                lightGrid[x][y] = 0;
            }
        }
        
        // Распространяем свет от источников
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                int element = grid[x][y];
                int lightLevel = 0;
                
                if (element == LAMP) {
                    lightLevel = 100; // Лампа дает сильный свет
                } else if (element == FIRE) {
                    lightLevel = 60; // Огонь дает средний свет
                } else if (element == LAVA) {
                    lightLevel = 40; // Лава дает слабый свет
                } else if (element == URANIUM && heatGrid[x][y] > 50) {
                    lightLevel = 30; // Нагретый уран светится
                }
                
                if (lightLevel > 0) {
                    spreadLight(x, y, lightLevel);
                }
            }
        }
    }
    
    private void spreadLight(int x, int y, int lightLevel) {
        if (lightLevel <= 0) return;
        
        // Устанавливаем свет в текущей клетке
        if (lightGrid[x][y] < lightLevel) {
            lightGrid[x][y] = lightLevel;
        }
        
        // Распространяем свет в соседние клетки
        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            
            if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS) {
                // Свет проходит через прозрачные материалы
                if (grid[nx][ny] == EMPTY || grid[nx][ny] == GLASS || 
                    grid[nx][ny] == WATER || grid[nx][ny] == SMOKE ||
                    grid[nx][ny] == NITROGEN) {
                    int newLightLevel = lightLevel - 15; // Свет ослабевает с расстоянием
                    if (newLightLevel > lightGrid[nx][ny]) {
                        spreadLight(nx, ny, newLightLevel);
                    }
                }
            }
        }
    }
    
    private void updateSand(int x, int y) {
        if (tryMove(x, y, 0, 1)) return;
        if (tryMoveDiagonal(x, y)) return;
    }
    
    private void updateWater(int x, int y) {
        if (tryMove(x, y, 0, 1)) return;
        if (tryFlow(x, y)) return;
        
        checkLavaInteraction(x, y, WATER, STONE);
        checkFireInteraction(x, y, WATER, SMOKE);
    }
    
    private void updateLava(int x, int y) {
        if (tryMove(x, y, 0, 1)) return;
        if (random.nextFloat() < 0.3f && tryFlow(x, y)) return;
        
        if (random.nextFloat() < 0.02f) {
            createFireAround(x, y);
        }
        
        // Охлаждение лавы в камень
        if (random.nextFloat() < 0.001f) {
            gridBuffer[x][y] = STONE;
        }
        
        // Нагрев металлов - РАЗНЫЕ СКОРОСТИ ДЛЯ РАЗНЫХ МАТЕРИАЛОВ
        heatMetal(x, y, IRON, MOLTEN_IRON, 0.8f);     // Железо плавится медленнее
        heatMetal(x, y, GOLD, MOLTEN_GOLD, 0.7f);     // Золото плавится еще медленнее
        heatMetal(x, y, COPPER, MOLTEN_COPPER, 0.9f); // Медь плавится быстрее
        heatMetal(x, y, STONE, LAVA, 1.5f);           // Камень плавится быстрее железа
        
        // Застывание от жидкого азота
        if (checkLiquidNitrogenNearby(x, y) && random.nextFloat() < 0.3f) {
            gridBuffer[x][y] = STONE;
        }
    }
    
    private void updateFire(int x, int y) {
        if (tryMove(x, y, 0, -1)) return;
        
        if (random.nextFloat() < 0.3f) {
            int dx = random.nextInt(3) - 1;
            if (tryMove(x, y, dx, -1)) return;
        }
        
        checkCombustibleMaterials(x, y);
        
        if (random.nextFloat() < 0.1f) {
            createSmokeAround(x, y);
        }
        
        // Нагрев металлов огнем
        heatMetal(x, y, IRON, MOLTEN_IRON, 0.3f);
        heatMetal(x, y, GOLD, MOLTEN_GOLD, 0.2f);
        heatMetal(x, y, COPPER, MOLTEN_COPPER, 0.4f);
        
        // Нагрев урана
        if (checkUraniumNearby(x, y) && random.nextFloat() < 0.1f) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = x + dx;
                    int ny = y + dy;
                    if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && 
                        gridBuffer[nx][ny] == URANIUM) {
                        heatGrid[nx][ny]++;
                        if (heatGrid[nx][ny] > 50) {
                            createNuclearExplosion(nx, ny);
                            gridBuffer[nx][ny] = EMPTY;
                        }
                    }
                }
            }
        }
        
        // 🔥 УВЕЛИЧЕНО ВРЕМЯ ГОРЕНИЯ ОГНЯ
        // Система "жизни" огня - горит дольше
        if (fireLifeGrid[x][y] > 0) {
            fireLifeGrid[x][y]--;
        } else {
            // Шанс исчезновения уменьшен в 3 раза
            if (random.nextFloat() < 0.03f) {  // Было 0.1f
                gridBuffer[x][y] = EMPTY;
            }
        }
    }
    
    private void updateEarth(int x, int y) {
        if (tryMove(x, y, 0, 1)) return;
    }
    
    private void updateStone(int x, int y) {
        // Камень неподвижен
    }
    
    private void updateSmoke(int x, int y) {
        if (y > 0 && gridBuffer[x][y - 1] == EMPTY) {
            gridBuffer[x][y] = EMPTY;
            gridBuffer[x][y - 1] = SMOKE;
            return;
        }
        
        if (random.nextFloat() < 0.4f) {
            int dx = random.nextBoolean() ? 1 : -1;
            int newX = x + dx;
            if (newX >= 0 && newX < COLS && gridBuffer[newX][y] == EMPTY) {
                gridBuffer[x][y] = EMPTY;
                gridBuffer[newX][y] = SMOKE;
                return;
            }
        }
        
        if (random.nextFloat() < 0.03f) {
            gridBuffer[x][y] = EMPTY;
        }
    }
    
    private void updateSeed(int x, int y) {
        if (tryMove(x, y, 0, 1)) return;
        
        if (y < ROWS - 1) {
            int below = gridBuffer[x][y + 1];
            if (below == EARTH) {
                boolean hasWater = checkWaterNearby(x, y);
                float growthChance = hasWater ? 0.02f : 0.005f;
                
                if (random.nextFloat() < growthChance) {
                    gridBuffer[x][y] = GRASS;
                }
            }
        }
    }
    
    private void updateGrass(int x, int y) {
        if (y > 0) {
            boolean hasEarthBelow = (y < ROWS - 1) && (gridBuffer[x][y + 1] == EARTH || gridBuffer[x][y + 1] == GRASS);
            boolean hasWater = checkWaterNearby(x, y);
            
            if (hasEarthBelow && gridBuffer[x][y - 1] == EMPTY) {
                float growthChance = hasWater ? 0.001f : 0.0002f;
                if (random.nextFloat() < growthChance) {
                    gridBuffer[x][y - 1] = GRASS;
                }
            }
        }
        
        if (random.nextFloat() < 0.001f) {
            int[][] directions = {{1, 0}, {-1, 0}, {0, 1}};
            for (int[] dir : directions) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS) {
                    if (gridBuffer[nx][ny] == EARTH) {
                        boolean hasWater = checkWaterNearby(nx, ny);
                        if (hasWater || random.nextFloat() < 0.3f) {
                            gridBuffer[nx][ny] = GRASS;
                        }
                    }
                }
            }
        }
        
        // 🔥 УВЕЛИЧЕНО ВРЕМЯ ГОРЕНИЯ ТРАВЫ
        if (checkFireNearby(x, y) && random.nextFloat() < 0.02f) {  // Было 0.05f
            gridBuffer[x][y] = FIRE;
            fireLifeGrid[x][y] = 30; // Трава горит дольше
            createSmokeAround(x, y);
        }
    }
    
    private void updateWood(int x, int y) {
        // 🔥 УВЕЛИЧЕНО ВРЕМЯ ГОРЕНИЯ ДЕРЕВА
        if (checkFireNearby(x, y) && random.nextFloat() < 0.01f) {  // Было 0.03f
            gridBuffer[x][y] = FIRE;
            fireLifeGrid[x][y] = 80; // Дерево горит значительно дольше
            createSmokeAround(x, y);
        }
    }
    
    private void updateIce(int x, int y) {
        if (y < ROWS - 1 && gridBuffer[x][y + 1] == WATER) {
            if (random.nextFloat() < 0.1f) {
                gridBuffer[x][y] = WATER;
                gridBuffer[x][y + 1] = ICE;
                return;
            }
        }
        
        if (checkFireNearby(x, y) || checkLavaNearby(x, y)) {
            gridBuffer[x][y] = WATER;
            return;
        }
        
        if (y < ROWS - 1 && (gridBuffer[x][y + 1] == LAVA || gridBuffer[x][y + 1] == OIL)) {
            if (random.nextFloat() < 0.05f) {
                gridBuffer[x][y] = gridBuffer[x][y + 1];
                gridBuffer[x][y + 1] = ICE;
            }
        }
    }
    
    private void updateOil(int x, int y) {
        if (tryMove(x, y, 0, 1)) return;
        if (tryFlow(x, y)) return;
        
        if (checkFireNearby(x, y) && random.nextFloat() < 0.3f) {
            gridBuffer[x][y] = FIRE;
            fireLifeGrid[x][y] = 40; // Масло горит долго
            createSmokeAround(x, y);
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int nx = x + i;
                    int ny = y + j;
                    if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && 
                        gridBuffer[nx][ny] == OIL) {
                        gridBuffer[nx][ny] = FIRE;
                        fireLifeGrid[nx][ny] = 40;
                    }
                }
            }
        }
    }
    
    private void updateIron(int x, int y) {
        // Железо - твердый металл, не двигается
        // Охлаждение расплавленного железа
        if (checkLiquidNitrogenNearby(x, y) && random.nextFloat() < 0.05f) {
            // Охлаждается, но остается железом
            if (heatGrid[x][y] > 0) {
                heatGrid[x][y]--;
            }
        }
    }
    
    private void updateNitrogen(int x, int y) {
        // Жидкий азот - течет как жидкость
        if (tryMove(x, y, 0, 1)) return;
        if (tryFlow(x, y)) return;
        
        // Застывание лавы
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS) {
                    if (gridBuffer[nx][ny] == LAVA && random.nextFloat() < 0.2f) {
                        gridBuffer[nx][ny] = STONE;
                    }
                    // Застывание расплавленных металлов
                    if ((gridBuffer[nx][ny] == MOLTEN_IRON || 
                         gridBuffer[nx][ny] == MOLTEN_GOLD || 
                         gridBuffer[nx][ny] == MOLTEN_COPPER) && random.nextFloat() < 0.1f) {
                        if (gridBuffer[nx][ny] == MOLTEN_IRON) gridBuffer[nx][ny] = IRON;
                        if (gridBuffer[nx][ny] == MOLTEN_GOLD) gridBuffer[nx][ny] = GOLD;
                        if (gridBuffer[nx][ny] == MOLTEN_COPPER) gridBuffer[nx][ny] = COPPER;
                        heatGrid[nx][ny] = 0;
                    }
                    // Заморозка воды
                    if (gridBuffer[nx][ny] == WATER && random.nextFloat() < 0.1f) {
                        gridBuffer[nx][ny] = ICE;
                    }
                    // Заморозка кислоты
                    if (gridBuffer[nx][ny] == ACID && random.nextFloat() < 0.05f) {
                        gridBuffer[nx][ny] = ICE;
                    }
                }
            }
        }
        
        // Испарение азота
        if (random.nextFloat() < 0.02f) {
            gridBuffer[x][y] = EMPTY;
        }
    }
    
    private void updateUnbreakable(int x, int y) {
        // Абсолютно нерушимая стена - ничего не делает
    }
    
    private void updateAcid(int x, int y) {
        // Кислота течет как вода
        if (tryMove(x, y, 0, 1)) return;
        if (tryFlow(x, y)) return;
        
        // Растворение большинства материалов (кроме нерушимой стены и резины)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS) {
                    int target = gridBuffer[nx][ny];
                    if (target != EMPTY && target != UNBREAKABLE && target != RUBBER && 
                        target != ACID && random.nextFloat() < 0.3f) {
                        gridBuffer[nx][ny] = EMPTY;
                    }
                }
            }
        }
    }
    
    private void updateGlass(int x, int y) {
        // Стекло - твердый прозрачный материал
        // Может плавиться от лавы
        if (checkLavaNearby(x, y) && random.nextFloat() < 0.05f) {
            gridBuffer[x][y] = LAVA;
        }
    }
    
    private void updateDynamite(int x, int y) {
        // Динамит - твердый, пока не взорвется
        if (checkFireNearby(x, y) || checkLavaNearby(x, y)) {
            createExplosion(x, y, 8);
            gridBuffer[x][y] = EMPTY;
        }
    }
    
    private void updateGold(int x, int y) {
        // Золото - твердый металл
        // Охлаждение
        if (checkLiquidNitrogenNearby(x, y) && random.nextFloat() < 0.05f) {
            if (heatGrid[x][y] > 0) {
                heatGrid[x][y]--;
            }
        }
    }
    
    private void updateCopper(int x, int y) {
        // Медь - твердый металл
        // Охлаждение
        if (checkLiquidNitrogenNearby(x, y) && random.nextFloat() < 0.05f) {
            if (heatGrid[x][y] > 0) {
                heatGrid[x][y]--;
            }
        }
    }
    
    private void updateSalt(int x, int y) {
        // Соль - сыпучий материал
        if (tryMove(x, y, 0, 1)) return;
        if (tryMoveDiagonal(x, y)) return;
        
        // Растворение в воде
        if (checkWaterNearby(x, y) && random.nextFloat() < 0.1f) {
            gridBuffer[x][y] = EMPTY;
        }
    }
    
    private void updateCement(int x, int y) {
        // Цемент - твердый материал
        if (tryMove(x, y, 0, 1)) return;
        
        // Затвердевание при контакте с водой
        if (checkWaterNearby(x, y) && random.nextFloat() < 0.01f) {
            gridBuffer[x][y] = STONE;
        }
    }
    
    private void updateRubber(int x, int y) {
        // Резина - упругий материал, не горит, устойчив к кислоте
        if (tryMove(x, y, 0, 1)) return;
    }
    
    private void updateGasoline(int x, int y) {
        // Бензин - очень горючая жидкость
        if (tryMove(x, y, 0, 1)) return;
        if (tryFlow(x, y)) return;
        
        if (checkFireNearby(x, y) && random.nextFloat() < 0.5f) {
            gridBuffer[x][y] = FIRE;
            fireLifeGrid[x][y] = 60; // Бензин горит очень долго
            createSmokeAround(x, y);
            // Создает большой взрыв
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    int nx = x + i;
                    int ny = y + j;
                    if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && 
                        gridBuffer[nx][ny] == GASOLINE) {
                        gridBuffer[nx][ny] = FIRE;
                        fireLifeGrid[nx][ny] = 60;
                    }
                }
            }
        }
    }
    
    private void updateMercury(int x, int y) {
        // Ртуть - тяжелая жидкость
        if (tryMove(x, y, 0, 1)) return;
        if (tryFlow(x, y)) return;
        
        // Ртуть не смешивается с водой
        if (y < ROWS - 1 && gridBuffer[x][y + 1] == WATER) {
            if (random.nextFloat() < 0.1f) {
                gridBuffer[x][y] = WATER;
                gridBuffer[x][y + 1] = MERCURY;
            }
        }
    }
    
    private void updateUranium(int x, int y) {
        // Уран - радиоактивный твердый материал
        // Постепенно нагревается сам по себе
        if (random.nextFloat() < 0.001f) {
            heatGrid[x][y]++;
        }
        
        // Взрыв при перегреве
        if (heatGrid[x][y] > 100) {
            createNuclearExplosion(x, y);
            gridBuffer[x][y] = EMPTY;
        }
        
        // Свечение при нагреве
        if (heatGrid[x][y] > 50 && random.nextFloat() < 0.1f) {
            createFireAround(x, y);
        }
    }
    
    private void updateMoltenIron(int x, int y) {
        // Расплавленное железо - течет как жидкость
        if (tryMove(x, y, 0, 1)) return;
        if (tryFlow(x, y)) return;
        
        // Охлаждение и застывание
        if (checkLiquidNitrogenNearby(x, y) && random.nextFloat() < 0.1f) {
            gridBuffer[x][y] = IRON;
            heatGrid[x][y] = 0;
        } else if (random.nextFloat() < 0.001f) {
            gridBuffer[x][y] = IRON;
            heatGrid[x][y] = 0;
        }
    }
    
    private void updateMoltenGold(int x, int y) {
        // Расплавленное золото - течет как жидкость
        if (tryMove(x, y, 0, 1)) return;
        if (tryFlow(x, y)) return;
        
        // Охлаждение и застывание
        if (checkLiquidNitrogenNearby(x, y) && random.nextFloat() < 0.1f) {
            gridBuffer[x][y] = GOLD;
            heatGrid[x][y] = 0;
        } else if (random.nextFloat() < 0.001f) {
            gridBuffer[x][y] = GOLD;
            heatGrid[x][y] = 0;
        }
    }
    
    private void updateMoltenCopper(int x, int y) {
        // Расплавленная медь - течет как жидкость
        if (tryMove(x, y, 0, 1)) return;
        if (tryFlow(x, y)) return;
        
        // Охлаждение и застывание
        if (checkLiquidNitrogenNearby(x, y) && random.nextFloat() < 0.1f) {
            gridBuffer[x][y] = COPPER;
            heatGrid[x][y] = 0;
        } else if (random.nextFloat() < 0.001f) {
            gridBuffer[x][y] = COPPER;
            heatGrid[x][y] = 0;
        }
    }
    
    private void updateLamp(int x, int y) {
        // Лампа - статичный светящийся элемент
        // Ничего не делает, просто светится
    }
    
    // Вспомогательные методы
    private boolean tryMove(int x, int y, int dx, int dy) {
        int newX = x + dx;
        int newY = y + dy;
        
        if (newX >= 0 && newX < COLS && newY >= 0 && newY < ROWS && 
            gridBuffer[newX][newY] == EMPTY) {
            gridBuffer[x][y] = EMPTY;
            gridBuffer[newX][newY] = grid[x][y];
            // Переносим тепло и жизнь огня
            heatGrid[newX][newY] = heatGrid[x][y];
            fireLifeGrid[newX][newY] = fireLifeGrid[x][y];
            heatGrid[x][y] = 0;
            fireLifeGrid[x][y] = 0;
            return true;
        }
        return false;
    }
    
    private boolean tryMoveDiagonal(int x, int y) {
        boolean left = x > 0 && gridBuffer[x - 1][y + 1] == EMPTY;
        boolean right = x < COLS - 1 && gridBuffer[x + 1][y + 1] == EMPTY;
        
        if (left && right) {
            if (random.nextBoolean()) {
                return tryMove(x, y, -1, 1);
            } else {
                return tryMove(x, y, 1, 1);
            }
        } else if (left) {
            return tryMove(x, y, -1, 1);
        } else if (right) {
            return tryMove(x, y, 1, 1);
        }
        return false;
    }
    
    private boolean tryFlow(int x, int y) {
        int[] directions = {-1, 1};
        if (random.nextBoolean()) {
            int temp = directions[0];
            directions[0] = directions[1];
            directions[1] = temp;
        }
        
        for (int dx : directions) {
            if (tryMove(x, y, dx, 0)) return true;
        }
        
        for (int dx : directions) {
            if (y > 0 && tryMove(x, y, dx, -1)) return true;
        }
        
        return false;
    }
    
    private void heatMetal(int x, int y, int solidMetal, int moltenMetal, float heatRate) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS) {
                    if (gridBuffer[nx][ny] == solidMetal && random.nextFloat() < heatRate) {
                        heatGrid[nx][ny]++;
                        if (heatGrid[nx][ny] > 30) {
                            gridBuffer[nx][ny] = moltenMetal;
                        }
                    }
                }
            }
        }
    }
    
    private boolean checkWaterNearby(int x, int y) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && 
                    gridBuffer[nx][ny] == WATER) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean checkFireNearby(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && 
                    (gridBuffer[nx][ny] == FIRE || gridBuffer[nx][ny] == LAVA)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean checkLavaNearby(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && 
                    gridBuffer[nx][ny] == LAVA) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean checkLiquidNitrogenNearby(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && 
                    gridBuffer[nx][ny] == NITROGEN) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean checkUraniumNearby(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && 
                    gridBuffer[nx][ny] == URANIUM) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void checkLavaInteraction(int x, int y, int element, int result) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS) {
                    if ((gridBuffer[x][y] == LAVA && gridBuffer[nx][ny] == element) ||
                        (gridBuffer[x][y] == element && gridBuffer[nx][ny] == LAVA)) {
                        gridBuffer[x][y] = result;
                        gridBuffer[nx][ny] = result;
                    }
                }
            }
        }
    }
    
    private void checkFireInteraction(int x, int y, int element, int result) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS) {
                    if ((gridBuffer[x][y] == FIRE && gridBuffer[nx][ny] == element) ||
                        (gridBuffer[x][y] == element && gridBuffer[nx][ny] == FIRE)) {
                        gridBuffer[x][y] = result;
                        gridBuffer[nx][ny] = result;
                    }
                }
            }
        }
    }
    
    private void checkCombustibleMaterials(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS) {
                    int neighbor = gridBuffer[nx][ny];
                    if ((neighbor == SAND || neighbor == EARTH || neighbor == GRASS || 
                         neighbor == WOOD || neighbor == SEED) && random.nextFloat() < 0.1f) {
                        gridBuffer[nx][ny] = FIRE;
                        fireLifeGrid[nx][ny] = 50; // Новый огонь получает время жизни
                    }
                }
            }
        }
    }
    
    private void createFireAround(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && 
                    gridBuffer[nx][ny] == EMPTY && random.nextFloat() < 0.3f) {
                    gridBuffer[nx][ny] = FIRE;
                    fireLifeGrid[nx][ny] = 40; // Огонь вокруг получает время жизни
                }
            }
        }
    }
    
    private void createSmokeAround(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && 
                    gridBuffer[nx][ny] == EMPTY && random.nextFloat() < 0.4f) {
                    gridBuffer[nx][ny] = SMOKE;
                }
            }
        }
    }
    
    private void createExplosion(int x, int y, int radius) {
        explosions.add(new Explosion(x, y, radius));
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    int nx = x + dx;
                    int ny = y + dy;
                    if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS) {
                        // Нерушимая стена не разрушается
                        if (gridBuffer[nx][ny] != UNBREAKABLE && 
                            gridBuffer[nx][ny] != STONE && gridBuffer[nx][ny] != IRON && 
                            gridBuffer[nx][ny] != GOLD && gridBuffer[nx][ny] != COPPER) {
                            gridBuffer[nx][ny] = EMPTY;
                        }
                        if (dx * dx + dy * dy >= (radius - 1) * (radius - 1)) {
                            if (random.nextFloat() < 0.3f) {
                                gridBuffer[nx][ny] = FIRE;
                                fireLifeGrid[nx][ny] = 60; // Огонь от взрыва горит долго
                            }
                            if (random.nextFloat() < 0.5f) {
                                createSmokeAround(nx, ny);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void createNuclearExplosion(int x, int y) {
        explosions.add(new Explosion(x, y, 15));
        
        for (int dx = -10; dx <= 10; dx++) {
            for (int dy = -10; dy <= 10; dy++) {
                if (dx * dx + dy * dy <= 100) {
                    int nx = x + dx;
                    int ny = y + dy;
                    if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS) {
                        // Нерушимая стена не разрушается даже ядерным взрывом
                        if (gridBuffer[nx][ny] != UNBREAKABLE) {
                            gridBuffer[nx][ny] = EMPTY;
                        }
                        if (dx * dx + dy * dy >= 64) {
                            if (random.nextFloat() < 0.5f) {
                                gridBuffer[nx][ny] = FIRE;
                                fireLifeGrid[nx][ny] = 100; // Ядерный огонь горит очень долго
                            }
                            if (random.nextFloat() < 0.7f) {
                                createSmokeAround(nx, ny);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void updateExplosions() {
        explosions.removeIf(explosion -> {
            explosion.update();
            return !explosion.isAlive();
        });
    }
    
    // Методы для сохранения/загрузки
    private void saveGame(String fileName) {
        try {
            File file = new File("saves/" + fileName + ".sand");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            
            oos.writeObject(grid);
            oos.close();
            fos.close();
            
            System.out.println("Игра сохранена: " + fileName);
            refreshSaveFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadGame(String fileName) {
        try {
            File file = new File("saves/" + fileName + ".sand");
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            
            grid = (int[][]) ois.readObject();
            ois.close();
            fis.close();
            
            explosions.clear();
            // Сбрасываем тепло и жизнь огня при загрузке
            heatGrid = new int[COLS][ROWS];
            fireLifeGrid = new int[COLS][ROWS];
            lightGrid = new int[COLS][ROWS];
            System.out.println("Игра загружена: " + fileName);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка загрузки файла: " + fileName, "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String[] getSaveFiles() {
        File savesDir = new File("saves");
        File[] files = savesDir.listFiles((dir, name) -> name.endsWith(".sand"));
        if (files == null) return new String[0];
        
        String[] saveFiles = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            saveFiles[i] = files[i].getName().replace(".sand", "");
        }
        return saveFiles;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Отрисовка элементов с освещением
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                int element = grid[x][y];
                if (element != EMPTY) {
                    Color color = getColorForElement(element);
                    
                    // Эффект нагрева для металлов и урана
                    if ((element == IRON || element == GOLD || element == COPPER || element == URANIUM) && 
                        heatGrid[x][y] > 0) {
                        float heatFactor = Math.min(1.0f, heatGrid[x][y] / 30.0f);
                        color = applyHeatEffect(color, heatFactor);
                    }
                    
                    // Эффект освещения
                    if (lightGrid[x][y] > 0) {
                        color = applyLightEffect(color, lightGrid[x][y]);
                    }
                    
                    g.setColor(color);
                    g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
        
        // Отрисовка взрывов
        for (Explosion explosion : explosions) {
            if (explosion.isAlive()) {
                float alpha = explosion.life / 20.0f;
                g.setColor(new Color(255, 165, 0, (int)(alpha * 255)));
                int size = (int)(explosion.radius * CELL_SIZE * 2 * alpha);
                g.fillOval(explosion.x * CELL_SIZE - size/2, explosion.y * CELL_SIZE - size/2, size, size);
            }
        }
        
        // Отрисовка UI
        g.setColor(Color.WHITE);
        g.drawString("Элемент: " + getElementName(currentElement) + " | Кисть: " + brushSize, 10, 20);
        g.drawString("Форма: " + (roundBrush ? "Круглая" : "Квадратная") + " | Скорость: " + simulationSpeed + "x", 10, 40);
        g.drawString("1-9,0,A-Z: элементы | +/-: размер | C: очистить | ПРОБЕЛ: пауза", 10, 60);
        g.drawString("Ctrl+A: круглая кисть | Ctrl+S: квадратная | Стрелки: скорость", 10, 80);
        g.drawString("Ctrl+X: сохранить | Ctrl+L: загрузить | J: лампа", 10, 100);
        
        // FPS
        g.drawString("FPS: " + fps, WIDTH - 80, 20);
        
        if (paused) {
            g.setColor(Color.RED);
            g.drawString("ПАУЗА", WIDTH - 60, 40);
        }
        
        // Меню сохранения
        if (showSaveMenu) {
            drawSaveMenu(g);
        }
        
        // Меню загрузки
        if (showLoadMenu) {
            drawLoadMenu(g);
        }
    }
    
    private Color applyHeatEffect(Color baseColor, float heatFactor) {
        int r = baseColor.getRed();
        int g = baseColor.getGreen();
        int b = baseColor.getBlue();
        
        // Добавляем красный оттенок при нагреве
        r = Math.min(255, r + (int)(100 * heatFactor));
        g = Math.max(0, g - (int)(50 * heatFactor));
        
        return new Color(r, g, b);
    }
    
    private Color applyLightEffect(Color baseColor, int lightLevel) {
        int r = baseColor.getRed();
        int g = baseColor.getGreen();
        int b = baseColor.getBlue();
        
        // Добавляем светлый оттенок в зависимости от уровня освещения
        float lightFactor = Math.min(1.0f, lightLevel / 100.0f);
        r = Math.min(255, r + (int)((255 - r) * lightFactor * 0.3f));
        g = Math.min(255, g + (int)((255 - g) * lightFactor * 0.3f));
        b = Math.min(255, b + (int)((255 - b) * lightFactor * 0.3f));
        
        return new Color(r, g, b);
    }
    
    private void drawSaveMenu(Graphics g) {
        // Полупрозрачный фон
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(100, 100, WIDTH - 200, HEIGHT - 200);
        
        g.setColor(Color.WHITE);
        g.drawString("МЕНЮ СОХРАНЕНИЯ", WIDTH/2 - 60, 130);
        g.drawString("Введите имя файла: " + saveFileName, 120, 160);
        g.drawString("Нажмите ENTER для сохранения", 120, 180);
        g.drawString("Нажмите ESC для отмены", 120, 200);
        
        // Список существующих сохранений
        g.drawString("Существующие сохранения:", 120, 230);
        for (int i = 0; i < saveFiles.length && i < 10; i++) {
            g.drawString((i + 1) + ". " + saveFiles[i], 120, 250 + i * 20);
        }
    }
    
    private void drawLoadMenu(Graphics g) {
        // Полупрозрачный фон
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(100, 100, WIDTH - 200, HEIGHT - 200);
        
        g.setColor(Color.WHITE);
        g.drawString("МЕНЮ ЗАГРУЗКИ", WIDTH/2 - 50, 130);
        g.drawString("Выберите сохранение для загрузки:", 120, 160);
        g.drawString("Нажмите ENTER для загрузки", 120, 180);
        g.drawString("Нажмите ESC для отмены", 120, 200);
        g.drawString("Стрелки ВВЕРХ/ВНИЗ для выбора", 120, 220);
        
        // Список сохранений
        g.drawString("Доступные сохранения:", 120, 250);
        for (int i = 0; i < saveFiles.length && i < 10; i++) {
            if (i == selectedSaveIndex) {
                g.setColor(Color.YELLOW);
                g.drawString("> " + saveFiles[i], 120, 280 + i * 20);
                g.setColor(Color.WHITE);
            } else {
                g.drawString((i + 1) + ". " + saveFiles[i], 120, 280 + i * 20);
            }
        }
        
        if (saveFiles.length == 0) {
            g.drawString("Нет сохраненных игр", 120, 280);
        }
    }
    
    private Color getColorForElement(int element) {
        switch (element) {
            case SAND: return new Color(240, 230, 140);
            case WATER: return new Color(30, 144, 255, 180);
            case LAVA: return new Color(255, 69, 0);
            case FIRE: 
                Color[] fireColors = {Color.RED, Color.ORANGE, Color.YELLOW};
                return fireColors[random.nextInt(fireColors.length)];
            case EARTH: return new Color(139, 69, 19);
            case STONE: return new Color(128, 128, 128);
            case SMOKE: return new Color(105, 105, 105, 180);
            case SEED: return new Color(34, 139, 34);
            case GRASS: return new Color(50, 205, 50);
            case WOOD: return new Color(101, 67, 33);
            case ICE: return new Color(200, 230, 255, 220);
            case OIL: return new Color(25, 25, 25);
            case IRON: return new Color(192, 192, 192);
            case NITROGEN: return new Color(70, 130, 180, 200); // Темно-синий для жидкого азота
            case UNBREAKABLE: return new Color(50, 50, 50);
            case ACID: return new Color(50, 255, 50, 200);
            case GLASS: return new Color(200, 200, 255, 100);
            case DYNAMITE: return new Color(178, 34, 34);
            case GOLD: return new Color(255, 215, 0);
            case COPPER: return new Color(184, 115, 51);
            case SALT: return new Color(255, 255, 255);
            case CEMENT: return new Color(210, 210, 210);
            case RUBBER: return new Color(40, 40, 40);
            case GASOLINE: return new Color(255, 255, 0, 150);
            case MERCURY: return new Color(220, 220, 220);
            case URANIUM: return new Color(0, 255, 0); // Ярко-зеленый для урана
            case MOLTEN_IRON: return new Color(255, 100, 0); // Оранжевый для расплавленного железа
            case MOLTEN_GOLD: return new Color(255, 200, 0); // Ярко-желтый для расплавленного золота
            case MOLTEN_COPPER: return new Color(255, 150, 50); // Оранжево-красный для расплавленной меди
            case LAMP: return new Color(255, 255, 200); // Светло-желтый для лампы
            case ERASER: return Color.WHITE;
            default: return Color.BLACK;
        }
    }
    
    private String getElementName(int element) {
        switch (element) {
            case SAND: return "Песок";
            case WATER: return "Вода";
            case LAVA: return "Лава";
            case FIRE: return "Огонь";
            case EARTH: return "Земля";
            case STONE: return "Камень";
            case SMOKE: return "Дым";
            case SEED: return "Семена";
            case GRASS: return "Трава";
            case WOOD: return "Дерево";
            case ICE: return "Лёд";
            case OIL: return "Масло";
            case IRON: return "Железо";
            case NITROGEN: return "Жидкий азот";
            case UNBREAKABLE: return "Нерушимая стена";
            case ACID: return "Кислота";
            case GLASS: return "Стекло";
            case DYNAMITE: return "Динамит";
            case GOLD: return "Золото";
            case COPPER: return "Медь";
            case SALT: return "Соль";
            case CEMENT: return "Цемент";
            case RUBBER: return "Резина";
            case GASOLINE: return "Бензин";
            case MERCURY: return "Ртуть";
            case URANIUM: return "Уран";
            case MOLTEN_IRON: return "Расплавленное железо";
            case MOLTEN_GOLD: return "Расплавленное золото";
            case MOLTEN_COPPER: return "Расплавленная медь";
            case LAMP: return "Лампа";
            case ERASER: return "Ластик";
            default: return "Пустота";
        }
    }
    
    private void placeElement(int x, int y) {
        if (showSaveMenu || showLoadMenu) return;
        
        int gridX = x / CELL_SIZE;
        int gridY = y / CELL_SIZE;
        
        for (int dx = -brushSize; dx <= brushSize; dx++) {
            for (int dy = -brushSize; dy <= brushSize; dy++) {
                int newX = gridX + dx;
                int newY = gridY + dy;
                
                if (newX >= 0 && newX < COLS && newY >= 0 && newY < ROWS) {
                    // 🔧 ВЫБОР ФОРМЫ КИСТИ
                    boolean shouldPlace = false;
                    if (roundBrush) {
                        // Круглая кисть
                        shouldPlace = (dx * dx + dy * dy <= brushSize * brushSize);
                    } else {
                        // Квадратная кисть
                        shouldPlace = (Math.abs(dx) <= brushSize && Math.abs(dy) <= brushSize);
                    }
                    
                    if (shouldPlace) {
                        if (currentElement == ERASER) {
                            grid[newX][newY] = EMPTY;
                            heatGrid[newX][newY] = 0;
                            fireLifeGrid[newX][newY] = 0;
                            lightGrid[newX][newY] = 0;
                        } else {
                            grid[newX][newY] = currentElement;
                            // Сбрасываем тепло для новых элементов
                            if (currentElement != URANIUM && 
                                currentElement != IRON && 
                                currentElement != GOLD && 
                                currentElement != COPPER) {
                                heatGrid[newX][newY] = 0;
                            }
                            // Устанавливаем время жизни для нового огня
                            if (currentElement == FIRE) {
                                fireLifeGrid[newX][newY] = 100; // Новый огонь горит долго
                            } else {
                                fireLifeGrid[newX][newY] = 0;
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Mouse events
    @Override
    public void mousePressed(MouseEvent e) {
        if (showSaveMenu || showLoadMenu) return;
        mousePressed = true;
        placeElement(e.getX(), e.getY());
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (showSaveMenu || showLoadMenu) return;
        if (mousePressed) {
            placeElement(e.getX(), e.getY());
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (showSaveMenu) {
            handleSaveMenuInput(e);
            return;
        }
        
        if (showLoadMenu) {
            handleLoadMenuInput(e);
            return;
        }
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_1: currentElement = SAND; break;
            case KeyEvent.VK_2: currentElement = WATER; break;
            case KeyEvent.VK_3: currentElement = LAVA; break;
            case KeyEvent.VK_4: currentElement = FIRE; break;
            case KeyEvent.VK_5: currentElement = EARTH; break;
            case KeyEvent.VK_6: currentElement = STONE; break;
            case KeyEvent.VK_7: currentElement = SMOKE; break;
            case KeyEvent.VK_8: currentElement = SEED; break;
            case KeyEvent.VK_9: currentElement = GRASS; break;
            case KeyEvent.VK_0: currentElement = ERASER; break;
            case KeyEvent.VK_Q: currentElement = WOOD; break;
            case KeyEvent.VK_W: currentElement = ICE; break;
            case KeyEvent.VK_E: currentElement = IRON; break;
            case KeyEvent.VK_R: currentElement = NITROGEN; break;
            case KeyEvent.VK_T: currentElement = UNBREAKABLE; break;
            case KeyEvent.VK_Y: currentElement = ACID; break;
            case KeyEvent.VK_U: currentElement = GLASS; break;
            case KeyEvent.VK_I: currentElement = DYNAMITE; break;
            case KeyEvent.VK_O: currentElement = GOLD; break;
            case KeyEvent.VK_P: currentElement = COPPER; break;
            case KeyEvent.VK_A: 
                if (e.isControlDown()) {
                    // 🔧 ПЕРЕКЛЮЧЕНИЕ НА КРУГЛУЮ КИСТЬ
                    roundBrush = true;
                } else {
                    currentElement = SALT;
                }
                break;
            case KeyEvent.VK_S: 
                if (e.isControlDown()) {
                    // 🔧 ПЕРЕКЛЮЧЕНИЕ НА КВАДРАТНУЮ КИСТЬ
                    roundBrush = false;
                } else {
                    currentElement = CEMENT;
                }
                break;
            case KeyEvent.VK_D: currentElement = RUBBER; break;
            case KeyEvent.VK_F: currentElement = GASOLINE; break;
            case KeyEvent.VK_G: currentElement = MERCURY; break;
            case KeyEvent.VK_H: currentElement = URANIUM; break;
            case KeyEvent.VK_J: currentElement = LAMP; break; // 🔦 НОВЫЙ ЭЛЕМЕНТ - ЛАМПА
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_EQUALS:
                brushSize = Math.min(15, brushSize + 1);
                break;
            case KeyEvent.VK_MINUS:
                brushSize = Math.max(1, brushSize - 1);
                break;
            case KeyEvent.VK_C:
                grid = new int[COLS][ROWS];
                heatGrid = new int[COLS][ROWS];
                fireLifeGrid = new int[COLS][ROWS];
                lightGrid = new int[COLS][ROWS];
                explosions.clear();
                break;
            case KeyEvent.VK_SPACE:
                paused = !paused;
                break;
            case KeyEvent.VK_TAB:
                // FPS уже отображается всегда
                break;
            case KeyEvent.VK_UP:
                simulationSpeed = Math.min(5.0f, simulationSpeed + 0.5f);
                break;
            case KeyEvent.VK_DOWN:
                simulationSpeed = Math.max(0.1f, simulationSpeed - 0.5f);
                break;
            case KeyEvent.VK_X:
                if (e.isControlDown()) {
                    // 💾 СОХРАНЕНИЕ ПО Ctrl+X
                    showSaveMenu = true;
                    saveFileName = "save_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    refreshSaveFiles();
                }
                break;
            case KeyEvent.VK_L:
            case KeyEvent.VK_V:
                if (e.isControlDown()) {
                    showLoadMenu = true;
                    refreshSaveFiles();
                    selectedSaveIndex = saveFiles.length > 0 ? 0 : -1;
                }
                break;
        }
    }
    
    private void handleSaveMenuInput(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                if (!saveFileName.trim().isEmpty()) {
                    saveGame(saveFileName);
                    showSaveMenu = false;
                    saveFileName = "";
                }
                break;
            case KeyEvent.VK_ESCAPE:
                showSaveMenu = false;
                saveFileName = "";
                break;
            case KeyEvent.VK_BACK_SPACE:
                if (saveFileName.length() > 0) {
                    saveFileName = saveFileName.substring(0, saveFileName.length() - 1);
                }
                break;
            default:
                if (Character.isLetterOrDigit(e.getKeyChar()) || e.getKeyChar() == '_' || e.getKeyChar() == '-') {
                    saveFileName += e.getKeyChar();
                }
                break;
        }
        repaint();
    }
    
    private void handleLoadMenuInput(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                if (selectedSaveIndex >= 0 && selectedSaveIndex < saveFiles.length) {
                    loadGame(saveFiles[selectedSaveIndex]);
                    showLoadMenu = false;
                    selectedSaveIndex = -1;
                }
                break;
            case KeyEvent.VK_ESCAPE:
                showLoadMenu = false;
                selectedSaveIndex = -1;
                break;
            case KeyEvent.VK_UP:
                if (saveFiles.length > 0) {
                    selectedSaveIndex = (selectedSaveIndex - 1 + saveFiles.length) % saveFiles.length;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (saveFiles.length > 0) {
                    selectedSaveIndex = (selectedSaveIndex + 1) % saveFiles.length;
                }
                break;
        }
        repaint();
    }
    
    // Остальные методы интерфейсов
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("PlusSandbox - Улучшенная Физическая Песочница");
        Main game = new Main();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        new Thread(game).start();
    }
}