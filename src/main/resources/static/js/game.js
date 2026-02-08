// 游戏状态
const gameState = {
    running: false,
    score: 0,
    lives: 3
};

// 画布和上下文
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');

// 马里奥对象
const mario = {
    x: 50,
    y: 300,
    width: 40,
    height: 50,
    velocityX: 0,
    velocityY: 0,
    speed: 5,
    jumpPower: -15,
    onGround: false,
    color: '#e74c3c'
};

// 平台数组
const platforms = [
    { x: 0, y: 350, width: 200, height: 50 },
    { x: 250, y: 320, width: 150, height: 50 },
    { x: 450, y: 280, width: 150, height: 50 },
    { x: 650, y: 300, width: 150, height: 50 },
    { x: 0, y: 350, width: 800, height: 50 } // 地面
];

// 敌人数组
const enemies = [];
let enemySpawnTimer = 0;

// 金币数组
const coins = [];
let coinSpawnTimer = 0;

// 按键状态
const keys = {};

// 初始化游戏
function init() {
    document.getElementById('startBtn').addEventListener('click', startGame);
    document.getElementById('restartBtn').addEventListener('click', restartGame);
    
    // 键盘事件
    document.addEventListener('keydown', (e) => {
        keys[e.code] = true;
        if (e.code === 'Space') {
            e.preventDefault();
        }
    });
    
    document.addEventListener('keyup', (e) => {
        keys[e.code] = false;
    });
    
    // 初始生成一些金币
    for (let i = 0; i < 5; i++) {
        spawnCoin();
    }
}

// 开始游戏
function startGame() {
    gameState.running = true;
    document.getElementById('startScreen').classList.add('hidden');
    gameLoop();
}

// 重新开始游戏
function restartGame() {
    gameState.running = true;
    gameState.score = 0;
    gameState.lives = 3;
    mario.x = 50;
    mario.y = 300;
    mario.velocityX = 0;
    mario.velocityY = 0;
    enemies.length = 0;
    coins.length = 0;
    enemySpawnTimer = 0;
    coinSpawnTimer = 0;
    
    // 重新生成金币
    for (let i = 0; i < 5; i++) {
        spawnCoin();
    }
    
    document.getElementById('gameOver').classList.add('hidden');
    updateUI();
    gameLoop();
}

// 生成敌人
function spawnEnemy() {
    enemies.push({
        x: canvas.width,
        y: 300,
        width: 30,
        height: 30,
        speed: 3,
        color: '#8b4513'
    });
}

// 生成金币
function spawnCoin() {
    const platform = platforms[Math.floor(Math.random() * (platforms.length - 1))];
    coins.push({
        x: platform.x + Math.random() * (platform.width - 20),
        y: platform.y - 30,
        width: 20,
        height: 20,
        collected: false,
        rotation: 0
    });
}

// 更新游戏
function update() {
    if (!gameState.running) return;
    
    // 马里奥移动
    if (keys['ArrowLeft']) {
        mario.velocityX = -mario.speed;
    } else if (keys['ArrowRight']) {
        mario.velocityX = mario.speed;
    } else {
        mario.velocityX *= 0.8; // 摩擦力
    }
    
    // 跳跃
    if (keys['Space'] && mario.onGround) {
        mario.velocityY = mario.jumpPower;
        mario.onGround = false;
    }
    
    // 重力
    mario.velocityY += 0.8;
    
    // 更新位置
    mario.x += mario.velocityX;
    mario.y += mario.velocityY;
    
    // 边界检测
    if (mario.x < 0) mario.x = 0;
    if (mario.x + mario.width > canvas.width) mario.x = canvas.width - mario.width;
    
    // 平台碰撞检测
    mario.onGround = false;
    for (let platform of platforms) {
        if (checkCollision(mario, platform)) {
            // 从上方落下
            if (mario.velocityY > 0 && mario.y < platform.y) {
                mario.y = platform.y - mario.height;
                mario.velocityY = 0;
                mario.onGround = true;
            }
            // 从下方碰撞
            else if (mario.velocityY < 0) {
                mario.y = platform.y + platform.height;
                mario.velocityY = 0;
            }
            // 左右碰撞
            if (mario.velocityX > 0) {
                mario.x = platform.x - mario.width;
            } else if (mario.velocityX < 0) {
                mario.x = platform.x + platform.width;
            }
        }
    }
    
    // 更新敌人
    enemySpawnTimer++;
    if (enemySpawnTimer > 180) {
        spawnEnemy();
        enemySpawnTimer = 0;
    }
    
    for (let i = enemies.length - 1; i >= 0; i--) {
        const enemy = enemies[i];
        enemy.x -= enemy.speed;
        
        // 敌人平台碰撞
        let enemyOnGround = false;
        for (let platform of platforms) {
            if (enemy.x + enemy.width > platform.x && 
                enemy.x < platform.x + platform.width &&
                enemy.y + enemy.height >= platform.y &&
                enemy.y < platform.y + 10) {
                enemy.y = platform.y - enemy.height;
                enemyOnGround = true;
            }
        }
        
        // 敌人掉出屏幕
        if (enemy.x + enemy.width < 0) {
            enemies.splice(i, 1);
            continue;
        }
        
        // 马里奥与敌人碰撞
        if (checkCollision(mario, enemy)) {
            // 从上方踩敌人
            if (mario.velocityY > 0 && mario.y < enemy.y) {
                enemies.splice(i, 1);
                mario.velocityY = -5; // 小弹跳
                gameState.score += 100;
            } else {
                // 被敌人碰到
                gameState.lives--;
                mario.x = 50;
                mario.y = 300;
                mario.velocityX = 0;
                mario.velocityY = 0;
                
                if (gameState.lives <= 0) {
                    gameOver();
                    return;
                }
            }
        }
    }
    
    // 更新金币
    coinSpawnTimer++;
    if (coinSpawnTimer > 300) {
        spawnCoin();
        coinSpawnTimer = 0;
    }
    
    for (let coin of coins) {
        if (!coin.collected) {
            coin.rotation += 0.1;
            
            // 收集金币
            if (checkCollision(mario, coin)) {
                coin.collected = true;
                gameState.score += 50;
            }
        }
    }
    
    // 移除已收集的金币
    coins.forEach((coin, index) => {
        if (coin.collected && coin.rotation > Math.PI * 2) {
            coins.splice(index, 1);
        }
    });
    
    updateUI();
}

// 碰撞检测
function checkCollision(rect1, rect2) {
    return rect1.x < rect2.x + rect2.width &&
           rect1.x + rect1.width > rect2.x &&
           rect1.y < rect2.y + rect2.height &&
           rect1.y + rect1.height > rect2.y;
}

// 绘制游戏
function draw() {
    // 清空画布
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // 绘制云朵背景
    drawClouds();
    
    // 绘制平台
    for (let platform of platforms) {
        ctx.fillStyle = '#8b4513';
        ctx.fillRect(platform.x, platform.y, platform.width, platform.height);
        
        // 平台顶部草
        ctx.fillStyle = '#2ecc71';
        ctx.fillRect(platform.x, platform.y, platform.width, 5);
    }
    
    // 绘制金币
    for (let coin of coins) {
        if (!coin.collected) {
            ctx.save();
            ctx.translate(coin.x + coin.width / 2, coin.y + coin.height / 2);
            ctx.rotate(coin.rotation);
            ctx.fillStyle = '#f1c40f';
            ctx.beginPath();
            ctx.arc(0, 0, coin.width / 2, 0, Math.PI * 2);
            ctx.fill();
            ctx.strokeStyle = '#f39c12';
            ctx.lineWidth = 2;
            ctx.stroke();
            ctx.restore();
        }
    }
    
    // 绘制敌人
    for (let enemy of enemies) {
        ctx.fillStyle = enemy.color;
        ctx.fillRect(enemy.x, enemy.y, enemy.width, enemy.height);
        
        // 敌人眼睛
        ctx.fillStyle = 'white';
        ctx.fillRect(enemy.x + 5, enemy.y + 5, 8, 8);
        ctx.fillRect(enemy.x + 17, enemy.y + 5, 8, 8);
        ctx.fillStyle = 'black';
        ctx.fillRect(enemy.x + 7, enemy.y + 7, 4, 4);
        ctx.fillRect(enemy.x + 19, enemy.y + 7, 4, 4);
    }
    
    // 绘制马里奥
    drawMario();
}

// 绘制马里奥
function drawMario() {
    ctx.fillStyle = mario.color;
    // 身体
    ctx.fillRect(mario.x + 5, mario.y + 20, 30, 30);
    
    // 头部
    ctx.fillStyle = '#ffdbac';
    ctx.fillRect(mario.x + 8, mario.y, 24, 25);
    
    // 帽子
    ctx.fillStyle = mario.color;
    ctx.fillRect(mario.x + 5, mario.y, 30, 8);
    
    // 眼睛
    ctx.fillStyle = 'black';
    ctx.fillRect(mario.x + 12, mario.y + 8, 4, 4);
    ctx.fillRect(mario.x + 24, mario.y + 8, 4, 4);
    
    // 胡子
    ctx.fillStyle = '#8b4513';
    ctx.fillRect(mario.x + 10, mario.y + 15, 20, 3);
    
    // 手臂
    ctx.fillStyle = '#ffdbac';
    ctx.fillRect(mario.x, mario.y + 25, 8, 15);
    ctx.fillRect(mario.x + 32, mario.y + 25, 8, 15);
    
    // 腿
    ctx.fillStyle = '#0000ff';
    ctx.fillRect(mario.x + 8, mario.y + 45, 12, 5);
    ctx.fillRect(mario.x + 20, mario.y + 45, 12, 5);
}

// 绘制云朵
function drawClouds() {
    ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
    for (let i = 0; i < 3; i++) {
        const x = (Date.now() / 50 + i * 300) % (canvas.width + 100) - 50;
        const y = 30 + i * 40;
        ctx.beginPath();
        ctx.arc(x, y, 20, 0, Math.PI * 2);
        ctx.arc(x + 25, y, 25, 0, Math.PI * 2);
        ctx.arc(x + 50, y, 20, 0, Math.PI * 2);
        ctx.fill();
    }
}

// 更新UI
function updateUI() {
    document.getElementById('score').textContent = gameState.score;
    document.getElementById('lives').textContent = gameState.lives;
}

// 游戏结束
function gameOver() {
    gameState.running = false;
    document.getElementById('finalScore').textContent = gameState.score;
    document.getElementById('gameOver').classList.remove('hidden');
}

// 游戏循环
function gameLoop() {
    if (!gameState.running) return;
    
    update();
    draw();
    
    requestAnimationFrame(gameLoop);
}

// 初始化
init();
