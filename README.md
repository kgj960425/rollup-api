# 보드게임 플랫폼 PRD (v2.0)

## 1. 프로젝트 개요

### 1.1 목적
다양한 보드게임을 플레이할 수 있는 웹 기반 멀티플레이어 플랫폼

### 1.2 지원 예정 게임

| 순서 | 게임 | 인원 | 타입 | 개발 우선순위 |
|------|------|------|------|--------------|
| 1 | **야추 다이스** | 1~4명 | 주사위 | 🔴 Phase 1 |
| 2 | 렉시오 | 2~4명 | 카드 | Phase 2 |
| 3 | 익스플로딩 키튼 | 2~5명 | 카드 | Phase 2 |
| 4 | 7원더스 듀얼 | 2명 | 카드/보드 | Phase 3 |
| 5 | 스플랜더 듀얼 | 2명 | 보드/토큰 | Phase 3 |

### 1.3 기술 스택 (변경 없음)

| 구분 | 기술 | 역할 |
|------|------|------|
| 프론트엔드 | React + Vite + TypeScript | UI, 실시간 구독 |
| 호스팅 | Firebase Hosting | 정적 파일 배포 |
| 인증 | Firebase Auth | 로그인, JWT |
| 실시간 상태 | Firestore | 게임 상태, 채팅 |
| Presence | Firebase Realtime DB | 온라인/오프라인 |
| 백엔드 | Java + Spring Boot | 게임 로직, 서버 권위 |
| 백엔드 호스팅 | Cloudtype | API 서버 |
| 영속 DB | Supabase PostgreSQL | 유저, 로그, 통계 |

---

## 2. 확장 가능한 아키텍처

### 2.1 전체 구조

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Firebase Hosting                                │
│                      React + Vite + TypeScript                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                         프론트엔드 구조                              │ │
│  │                                                                    │ │
│  │  src/                                                              │ │
│  │  ├── features/                                                     │ │
│  │  │   ├── auth/           # 인증                                    │ │
│  │  │   ├── lobby/          # 로비 (게임 선택)                         │ │
│  │  │   ├── room/           # 대기실 (공통)                            │ │
│  │  │   └── games/          # 게임별 UI                               │ │
│  │  │       ├── common/     # 공통 컴포넌트                            │ │
│  │  │       ├── yacht/      # 야추 다이스                              │ │
│  │  │       ├── lexio/      # 렉시오                                  │ │
│  │  │       ├── exploding/  # 익스플로딩 키튼                          │ │
│  │  │       ├── 7wonders/   # 7원더스 듀얼                            │ │
│  │  │       └── splendor/   # 스플랜더 듀얼                            │ │
│  │  │                                                                 │ │
│  └──┼─────────────────────────────────────────────────────────────────┘ │
└─────┼───────────────────────────────────────────────────────────────────┘
      │
      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        Cloudtype (Spring Boot)                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                         백엔드 구조                                  │ │
│  │                                                                    │ │
│  │  com.boardgame/                                                    │ │
│  │  ├── common/              # 공통 모듈                               │ │
│  │  │   ├── controller/      # 로비, 방, 유저 API                      │ │
│  │  │   ├── service/         # 공통 서비스                             │ │
│  │  │   └── model/           # 공통 모델                               │ │
│  │  │                                                                 │ │
│  │  └── games/               # 게임 엔진 (플러그인 구조)                │ │
│  │      ├── core/            # 게임 엔진 인터페이스                     │ │
│  │      │   ├── GameEngine.java         # 인터페이스                  │ │
│  │      │   ├── GameState.java          # 추상 클래스                 │ │
│  │      │   └── GameAction.java         # 액션 인터페이스              │ │
│  │      │                                                             │ │
│  │      ├── yacht/           # 야추 다이스 구현                        │ │
│  │      ├── lexio/           # 렉시오 구현                             │ │
│  │      ├── exploding/       # 익스플로딩 키튼 구현                     │ │
│  │      ├── sevenwonders/    # 7원더스 듀얼 구현                       │ │
│  │      └── splendor/        # 스플랜더 듀얼 구현                       │ │
│  │                                                                    │ │
│  └────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 게임 엔진 플러그인 구조 (핵심)

```
┌─────────────────────────────────────────────────────────────────┐
│                      GameEngine 인터페이스                       │
│                                                                 │
│  + getGameType(): String                                        │
│  + getMinPlayers(): int                                         │
│  + getMaxPlayers(): int                                         │
│  + initGame(players): GameState                                 │
│  + validateAction(state, action): ValidationResult              │
│  + applyAction(state, action): GameState                        │
│  + checkGameEnd(state): GameResult?                             │
│  + getPublicState(state): PublicState                           │
│  + getPrivateState(state, playerId): PrivateState               │
│                                                                 │
└───────────────────────────┬─────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│ YachtEngine   │   │ LexioEngine   │   │ExplodingEngine│
│               │   │               │   │               │
│ 야추 규칙 구현  │   │ 렉시오 규칙 구현│   │ 익스플로딩 구현 │
└───────────────┘   └───────────────┘   └───────────────┘
```

---

## 3. 데이터 모델 (멀티 게임 지원)

### 3.1 Firestore 구조

```
firestore/
│
├── games/                           # 게임 메타 정보 (정적)
│   └── {gameType}/
│       {
│         name: "야추 다이스"
│         description: "주사위 5개로..."
│         minPlayers: 1
│         maxPlayers: 4
│         thumbnailUrl: "..."
│         isActive: true
│       }
│
├── rooms/{roomId}/
│   │
│   ├── info                         # 방 기본 정보
│   │   {
│   │     gameType: "yacht"          # ⭐ 어떤 게임인지
│   │     hostId: string
│   │     maxPlayers: number
│   │     status: "waiting" | "playing" | "finished"
│   │     createdAt: timestamp
│   │     players: [
│   │       { id, nickname, isReady, isConnected }
│   │     ]
│   │   }
│   │
│   ├── state                        # 게임 상태 (게임마다 다름)
│   │   {
│   │     # 야추의 경우
│   │     currentPlayerId: string
│   │     turnIndex: number
│   │     round: number
│   │     diceValues: [1, 3, 3, 5, 6]
│   │     rollCount: 2
│   │     heldDice: [false, true, true, false, false]
│   │     scoreboards: {
│   │       "userId1": { ones: 3, twos: null, ... },
│   │       "userId2": { ones: null, twos: 6, ... }
│   │     }
│   │   }
│   │
│   ├── private/{userId}             # 비공개 정보 (게임에 따라)
│   │   {
│   │     # 렉시오의 경우: 손패
│   │     # 야추는 비공개 정보 없음
│   │   }
│   │
│   └── chat/{messageId}             # 채팅
│       {
│         userId: string
│         nickname: string
│         text: string
│         timestamp: timestamp
│       }
```

### 3.2 Realtime Database (Presence)

```
realtime-db/
│
└── rooms/{roomId}/presence/{userId}
    {
      online: boolean
      lastSeen: timestamp
    }
```

### 3.3 Supabase 테이블

```sql
-- 게임 타입 (정적 마스터)
CREATE TABLE game_types (
    id TEXT PRIMARY KEY,              -- 'yacht', 'lexio', ...
    name TEXT NOT NULL,               -- '야추 다이스'
    description TEXT,
    min_players INT NOT NULL,
    max_players INT NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 초기 데이터
INSERT INTO game_types (id, name, min_players, max_players) VALUES
('yacht', '야추 다이스', 1, 4),
('lexio', '렉시오', 2, 4),
('exploding', '익스플로딩 키튼', 2, 5),
('7wonders', '7원더스 듀얼', 2, 2),
('splendor', '스플랜더 듀얼', 2, 2);

-- 유저
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid TEXT UNIQUE NOT NULL,
    nickname TEXT NOT NULL,
    email TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 유저별 게임별 통계
CREATE TABLE user_game_stats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    game_type TEXT REFERENCES game_types(id),
    games_played INT DEFAULT 0,
    games_won INT DEFAULT 0,
    total_score BIGINT DEFAULT 0,
    best_score INT,
    
    UNIQUE(user_id, game_type)
);

-- 게임 로그
CREATE TABLE game_logs (
    id BIGSERIAL PRIMARY KEY,
    room_id TEXT NOT NULL,
    game_type TEXT REFERENCES game_types(id),
    user_id UUID REFERENCES users(id),
    event_type TEXT NOT NULL,
    event_data JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 게임 결과
CREATE TABLE game_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id TEXT NOT NULL,
    game_type TEXT REFERENCES game_types(id),
    winner_id UUID REFERENCES users(id),
    players JSONB,                    -- [{userId, nickname, score, rank}, ...]
    duration_seconds INT,
    finished_at TIMESTAMPTZ DEFAULT NOW()
);

-- 인덱스
CREATE INDEX idx_user_game_stats ON user_game_stats(user_id, game_type);
CREATE INDEX idx_game_logs_room ON game_logs(room_id, created_at);
CREATE INDEX idx_game_results_type ON game_results(game_type, finished_at);
```

---

## 4. API 설계

### 4.1 게임 메타

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/games` | 활성화된 게임 목록 |
| GET | `/api/games/{gameType}` | 게임 상세 정보 |

### 4.2 로비

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/rooms?gameType={gameType}` | 게임별 대기 방 목록 |
| POST | `/api/rooms` | 방 생성 (gameType 포함) |
| POST | `/api/rooms/{roomId}/join` | 방 입장 |
| POST | `/api/rooms/{roomId}/leave` | 방 퇴장 |

### 4.3 대기실

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/rooms/{roomId}/ready` | 준비 토글 |
| POST | `/api/rooms/{roomId}/start` | 게임 시작 |
| POST | `/api/rooms/{roomId}/kick/{userId}` | 강퇴 |

### 4.4 게임 액션 (통합 엔드포인트)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/game/{roomId}/action` | **모든 게임 액션 통합** |

```json
// 야추: 주사위 굴리기
{
  "actionType": "ROLL_DICE",
  "payload": {
    "heldDice": [false, true, true, false, false]
  }
}

// 야추: 점수 기록
{
  "actionType": "SCORE",
  "payload": {
    "category": "fullHouse"
  }
}

// 렉시오: 카드 내기
{
  "actionType": "PLAY_CARDS",
  "payload": {
    "cardIds": ["card1", "card2"]
  }
}

// 익스플로딩: 카드 뽑기
{
  "actionType": "DRAW_CARD",
  "payload": {}
}
```

### 4.5 유저/통계

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/users/me` | 내 정보 |
| GET | `/api/users/me/stats` | 전체 게임 통계 |
| GET | `/api/users/me/stats/{gameType}` | 특정 게임 통계 |
| GET | `/api/leaderboard/{gameType}` | 게임별 랭킹 |

---

## 5. Spring Boot 프로젝트 구조 (상세)

```
src/main/java/com/boardgame/
│
├── BoardGameApplication.java
│
├── config/
│   ├── FirebaseConfig.java
│   ├── SecurityConfig.java
│   └── GameEngineConfig.java          # 게임 엔진 등록
│
├── common/
│   ├── controller/
│   │   ├── GameMetaController.java    # GET /api/games
│   │   ├── RoomController.java        # 방 CRUD
│   │   ├── UserController.java        # 유저/통계
│   │   └── GameActionController.java  # POST /api/game/{roomId}/action
│   │
│   ├── service/
│   │   ├── RoomService.java
│   │   ├── UserService.java
│   │   ├── LogService.java
│   │   └── GameDispatcherService.java # ⭐ 게임 타입별 분기
│   │
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── GameLogRepository.java
│   │   └── GameResultRepository.java
│   │
│   └── dto/
│       ├── request/
│       │   ├── CreateRoomRequest.java
│       │   └── GameActionRequest.java
│       └── response/
│           ├── RoomResponse.java
│           └── GameStateResponse.java
│
└── games/
    │
    ├── core/                          # ⭐ 게임 엔진 추상화
    │   ├── GameEngine.java            # 인터페이스
    │   ├── GameState.java             # 추상 클래스
    │   ├── GameAction.java            # 액션 인터페이스
    │   ├── GameResult.java
    │   ├── ValidationResult.java
    │   └── GameRegistry.java          # 게임 엔진 등록/조회
    │
    ├── yacht/                         # 야추 다이스
    │   ├── YachtEngine.java
    │   ├── YachtState.java
    │   ├── YachtAction.java
    │   ├── YachtScoreCalculator.java
    │   └── dto/
    │       ├── YachtPublicState.java
    │       └── YachtPrivateState.java
    │
    ├── lexio/                         # 렉시오
    │   ├── LexioEngine.java
    │   ├── LexioState.java
    │   ├── LexioAction.java
    │   ├── LexioCardValidator.java
    │   └── dto/
    │
    ├── exploding/                     # 익스플로딩 키튼
    │   ├── ExplodingEngine.java
    │   ├── ExplodingState.java
    │   └── ...
    │
    ├── sevenwonders/                  # 7원더스 듀얼
    │   └── ...
    │
    └── splendor/                      # 스플랜더 듀얼
        └── ...
```

---

## 6. 핵심 코드 (게임 엔진 추상화)

### 6.1 GameEngine 인터페이스

```java
public interface GameEngine {
    
    // 게임 메타 정보
    String getGameType();
    int getMinPlayers();
    int getMaxPlayers();
    
    // 게임 초기화
    GameState initGame(List<Player> players, long seed);
    
    // 액션 검증
    ValidationResult validateAction(GameState state, String playerId, GameAction action);
    
    // 액션 적용
    GameState applyAction(GameState state, String playerId, GameAction action);
    
    // 종료 체크
    Optional<GameResult> checkGameEnd(GameState state);
    
    // 상태 직렬화 (Firestore 저장용)
    Map<String, Object> getPublicState(GameState state);
    Map<String, Object> getPrivateState(GameState state, String playerId);
    
    // JSON → Action 변환
    GameAction parseAction(String actionType, Map<String, Object> payload);
}
```

### 6.2 GameRegistry (게임 등록/조회)

```java
@Component
public class GameRegistry {
    
    private final Map<String, GameEngine> engines = new HashMap<>();
    
    // 생성자 주입으로 모든 GameEngine 구현체 자동 등록
    public GameRegistry(List<GameEngine> engineList) {
        for (GameEngine engine : engineList) {
            engines.put(engine.getGameType(), engine);
        }
    }
    
    public GameEngine getEngine(String gameType) {
        GameEngine engine = engines.get(gameType);
        if (engine == null) {
            throw new IllegalArgumentException("Unknown game type: " + gameType);
        }
        return engine;
    }
    
    public List<String> getAvailableGames() {
        return new ArrayList<>(engines.keySet());
    }
}
```

### 6.3 GameDispatcherService (액션 분기 처리)

```java
@Service
@RequiredArgsConstructor
public class GameDispatcherService {
    
    private final GameRegistry gameRegistry;
    private final FirestoreService firestoreService;
    private final LogService logService;
    
    public void handleAction(String roomId, String userId, GameActionRequest request) {
        // 1. 방 정보 조회
        RoomInfo room = firestoreService.getRoomInfo(roomId);
        String gameType = room.getGameType();
        
        // 2. 해당 게임 엔진 가져오기
        GameEngine engine = gameRegistry.getEngine(gameType);
        
        // 3. 현재 상태 조회
        GameState currentState = firestoreService.getGameState(roomId, gameType);
        
        // 4. 액션 파싱
        GameAction action = engine.parseAction(request.getActionType(), request.getPayload());
        
        // 5. 검증
        ValidationResult validation = engine.validateAction(currentState, userId, action);
        if (!validation.isValid()) {
            throw new InvalidActionException(validation.getMessage());
        }
        
        // 6. 액션 적용
        GameState newState = engine.applyAction(currentState, userId, action);
        
        // 7. Firestore 업데이트
        firestoreService.updateGameState(roomId, engine.getPublicState(newState));
        
        // 비공개 상태 업데이트 (해당 게임에 있다면)
        for (Player player : room.getPlayers()) {
            Map<String, Object> privateState = engine.getPrivateState(newState, player.getId());
            if (!privateState.isEmpty()) {
                firestoreService.updatePrivateState(roomId, player.getId(), privateState);
            }
        }
        
        // 8. 종료 체크
        Optional<GameResult> result = engine.checkGameEnd(newState);
        if (result.isPresent()) {
            handleGameEnd(roomId, room, result.get());
        }
        
        // 9. 로그 저장 (비동기)
        logService.saveAsync(roomId, gameType, userId, request.getActionType(), request.getPayload());
    }
}
```

### 6.4 야추 다이스 구현 예시

```java
@Component
public class YachtEngine implements GameEngine {
    
    @Override
    public String getGameType() {
        return "yacht";
    }
    
    @Override
    public int getMinPlayers() { return 1; }
    
    @Override
    public int getMaxPlayers() { return 4; }
    
    @Override
    public GameState initGame(List<Player> players, long seed) {
        YachtState state = new YachtState();
        state.setPlayers(players);
        state.setCurrentPlayerIndex(0);
        state.setRound(1);
        state.setDiceValues(new int[]{0, 0, 0, 0, 0});
        state.setHeldDice(new boolean[]{false, false, false, false, false});
        state.setRollCount(0);
        state.setRandom(new Random(seed));
        
        // 각 플레이어 점수판 초기화
        Map<String, YachtScoreboard> scoreboards = new HashMap<>();
        for (Player player : players) {
            scoreboards.put(player.getId(), new YachtScoreboard());
        }
        state.setScoreboards(scoreboards);
        
        return state;
    }
    
    @Override
    public ValidationResult validateAction(GameState state, String playerId, GameAction action) {
        YachtState yachtState = (YachtState) state;
        YachtAction yachtAction = (YachtAction) action;
        
        // 현재 턴인지 확인
        Player currentPlayer = yachtState.getCurrentPlayer();
        if (!currentPlayer.getId().equals(playerId)) {
            return ValidationResult.invalid("Not your turn");
        }
        
        switch (yachtAction.getType()) {
            case ROLL_DICE:
                if (yachtState.getRollCount() >= 3) {
                    return ValidationResult.invalid("No more rolls");
                }
                break;
                
            case SCORE:
                if (yachtState.getRollCount() == 0) {
                    return ValidationResult.invalid("Must roll first");
                }
                String category = yachtAction.getCategory();
                YachtScoreboard board = yachtState.getScoreboards().get(playerId);
                if (board.isScored(category)) {
                    return ValidationResult.invalid("Category already scored");
                }
                break;
        }
        
        return ValidationResult.valid();
    }
    
    @Override
    public GameState applyAction(GameState state, String playerId, GameAction action) {
        YachtState yachtState = ((YachtState) state).copy();
        YachtAction yachtAction = (YachtAction) action;
        
        switch (yachtAction.getType()) {
            case ROLL_DICE:
                rollDice(yachtState, yachtAction.getHeldDice());
                break;
                
            case SCORE:
                scoreCategory(yachtState, playerId, yachtAction.getCategory());
                nextTurn(yachtState);
                break;
        }
        
        return yachtState;
    }
    
    private void rollDice(YachtState state, boolean[] heldDice) {
        int[] dice = state.getDiceValues();
        Random random = state.getRandom();
        
        for (int i = 0; i < 5; i++) {
            if (!heldDice[i]) {
                dice[i] = random.nextInt(6) + 1;
            }
        }
        
        state.setDiceValues(dice);
        state.setHeldDice(heldDice);
        state.setRollCount(state.getRollCount() + 1);
    }
    
    private void scoreCategory(YachtState state, String playerId, String category) {
        int[] dice = state.getDiceValues();
        int score = YachtScoreCalculator.calculate(dice, category);
        
        YachtScoreboard board = state.getScoreboards().get(playerId);
        board.setScore(category, score);
    }
    
    private void nextTurn(YachtState state) {
        state.setRollCount(0);
        state.setHeldDice(new boolean[]{false, false, false, false, false});
        
        int nextIndex = (state.getCurrentPlayerIndex() + 1) % state.getPlayers().size();
        state.setCurrentPlayerIndex(nextIndex);
        
        // 한 바퀴 돌았으면 라운드 증가
        if (nextIndex == 0) {
            state.setRound(state.getRound() + 1);
        }
    }
    
    @Override
    public Optional<GameResult> checkGameEnd(GameState state) {
        YachtState yachtState = (YachtState) state;
        
        // 12라운드 끝났는지 (모든 카테고리 채움)
        if (yachtState.getRound() > 12) {
            return Optional.of(calculateFinalResult(yachtState));
        }
        
        return Optional.empty();
    }
    
    @Override
    public Map<String, Object> getPublicState(GameState state) {
        YachtState yachtState = (YachtState) state;
        
        Map<String, Object> publicState = new HashMap<>();
        publicState.put("currentPlayerId", yachtState.getCurrentPlayer().getId());
        publicState.put("round", yachtState.getRound());
        publicState.put("diceValues", yachtState.getDiceValues());
        publicState.put("heldDice", yachtState.getHeldDice());
        publicState.put("rollCount", yachtState.getRollCount());
        publicState.put("scoreboards", yachtState.getScoreboards());
        publicState.put("players", yachtState.getPlayers().stream()
            .map(p -> Map.of("id", p.getId(), "nickname", p.getNickname()))
            .toList());
        
        return publicState;
    }
    
    @Override
    public Map<String, Object> getPrivateState(GameState state, String playerId) {
        // 야추는 비공개 정보 없음 (모든 정보가 공개)
        return Collections.emptyMap();
    }
    
    @Override
    public GameAction parseAction(String actionType, Map<String, Object> payload) {
        YachtAction action = new YachtAction();
        action.setType(YachtActionType.valueOf(actionType));
        
        if (actionType.equals("ROLL_DICE")) {
            List<Boolean> held = (List<Boolean>) payload.get("heldDice");
            action.setHeldDice(toPrimitiveBooleanArray(held));
        } else if (actionType.equals("SCORE")) {
            action.setCategory((String) payload.get("category"));
        }
        
        return action;
    }
}
```

---

## 7. 프론트엔드 구조

```
src/
├── main.tsx
├── App.tsx
├── routes.tsx
│
├── components/                    # 공통 UI 컴포넌트
│   ├── Button.tsx
│   ├── Modal.tsx
│   ├── Avatar.tsx
│   └── ...
│
├── hooks/                         # 공통 훅
│   ├── useAuth.ts
│   ├── usePresence.ts
│   └── useFirestore.ts
│
├── features/
│   ├── auth/
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   └── useAuth.ts
│   │
│   ├── lobby/
│   │   ├── LobbyPage.tsx          # 게임 선택 화면
│   │   ├── GameSelectCard.tsx
│   │   └── RoomListPage.tsx       # 특정 게임의 방 목록
│   │
│   ├── room/
│   │   ├── WaitingRoom.tsx        # 대기실 (공통)
│   │   ├── PlayerList.tsx
│   │   ├── ChatBox.tsx
│   │   └── useRoom.ts
│   │
│   └── games/
│       ├── common/
│       │   ├── GameLayout.tsx     # 게임 공통 레이아웃
│       │   ├── PlayerStatus.tsx
│       │   ├── TurnIndicator.tsx
│       │   └── GameResult.tsx
│       │
│       ├── yacht/                 # 야추 다이스
│       │   ├── YachtGame.tsx
│       │   ├── DiceArea.tsx
│       │   ├── Scoreboard.tsx
│       │   ├── ScoreSelector.tsx
│       │   └── useYachtGame.ts
│       │
│       ├── lexio/                 # 렉시오
│       │   ├── LexioGame.tsx
│       │   ├── CardHand.tsx
│       │   ├── PlayArea.tsx
│       │   └── useLexioGame.ts
│       │
│       └── ...                    # 다른 게임들
│
├── services/
│   ├── firebase.ts
│   ├── api.ts                     # REST API 호출
│   └── gameApi.ts                 # 게임 액션 API
│
└── types/
    ├── common.ts
    ├── yacht.ts
    ├── lexio.ts
    └── ...
```

---

## 8. 라우팅

```typescript
// routes.tsx
const routes = [
  { path: "/", element: <LobbyPage /> },                    // 게임 선택
  { path: "/login", element: <LoginPage /> },
  { path: "/register", element: <RegisterPage /> },
  { path: "/games/:gameType", element: <RoomListPage /> },  // 방 목록
  { path: "/room/:roomId", element: <WaitingRoom /> },      // 대기실
  { path: "/play/:roomId", element: <GameRouter /> },       // 게임 플레이
  { path: "/profile", element: <ProfilePage /> },
];

// GameRouter.tsx - 게임 타입에 따라 다른 컴포넌트 렌더링
function GameRouter() {
  const { roomId } = useParams();
  const { room } = useRoom(roomId);
  
  switch (room?.gameType) {
    case "yacht":
      return <YachtGame roomId={roomId} />;
    case "lexio":
      return <LexioGame roomId={roomId} />;
    case "exploding":
      return <ExplodingGame roomId={roomId} />;
    default:
      return <div>Unknown game</div>;
  }
}
```

---

## 9. 개발 마일스톤 (수정)

### Phase 1: 플랫폼 기반 + 야추 (3~4주)

| 주차 | 작업 |
|------|------|
| 1주 | Firebase/Supabase 설정, 프로젝트 초기화, 인증 |
| 2주 | 로비, 방 생성/입장, 대기실, Presence |
| 3주 | 게임 엔진 추상화, 야추 엔진 구현 |
| 4주 | 야추 UI, 테스트, 배포 |

### Phase 2: 카드 게임 추가 (4~6주)

| 작업 |
|------|
| 렉시오 엔진 + UI |
| 익스플로딩 키튼 엔진 + UI |
| 공통 카드 컴포넌트 |

### Phase 3: 듀얼 게임 추가 (4~6주)

| 작업 |
|------|
| 7원더스 듀얼 엔진 + UI |
| 스플랜더 듀얼 엔진 + UI |

---

## 10. 새 게임 추가 체크리스트

새로운 게임을 추가할 때:

**백엔드**
- [ ] `games/{gameName}/` 폴더 생성
- [ ] `GameEngine` 인터페이스 구현
- [ ] `GameState` 클래스 구현
- [ ] `GameAction` 클래스 구현
- [ ] `@Component` 붙이면 자동 등록됨

**프론트엔드**
- [ ] `features/games/{gameName}/` 폴더 생성
- [ ] 게임 컴포넌트 구현
- [ ] `GameRouter`에 case 추가
- [ ] 타입 정의

**데이터**
- [ ] `game_types` 테이블에 INSERT
- [ ] Firestore `games/{gameType}` 문서 추가

---

이 구조면 새 게임 추가할 때 기존 코드 수정 최소화하고, 각 게임이 독립적으로 동작해.

다음 뭐 할까?

1. **Firebase 프로젝트 설정**
2. **Spring Boot 프로젝트 생성** (게임 엔진 추상화 포함)
3. **React 프로젝트 생성**
4. **야추 다이스 상세 설계** (점수 계산, UI 등)

# 개발 순서 (Development Roadmap)

## 전체 타임라인

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            Phase 1: 기반 + 야추 (4주)                        │
├─────────────┬─────────────┬─────────────┬─────────────┬─────────────────────┤
│   1주차     │   2주차     │    3주차    │    4주차    │                     │
│  환경설정   │   로비      │   게임엔진   │   야추 UI   │                     │
│  + 인증     │  + 대기실   │   + 야추    │  + 테스트   │                     │
├─────────────┴─────────────┴─────────────┴─────────────┼─────────────────────┤
│                            Phase 2: 카드게임 (4주)     │                     │
├─────────────┬─────────────┬─────────────┬─────────────┤                     │
│   5주차     │   6주차     │    7주차    │    8주차    │                     │
│  렉시오     │   렉시오    │  익스플로딩  │ 익스플로딩  │                     │
│  엔진       │    UI      │    엔진     │    UI      │                     │
├─────────────┴─────────────┴─────────────┴─────────────┼─────────────────────┤
│                            Phase 3: 듀얼게임 (4주)     │                     │
├─────────────┬─────────────┬─────────────┬─────────────┤                     │
│   9주차     │  10주차     │   11주차    │   12주차    │                     │
│ 7원더스듀얼 │ 7원더스듀얼 │스플랜더듀얼 │스플랜더듀얼 │                     │
│   엔진      │    UI      │    엔진     │    UI      │                     │
└─────────────┴─────────────┴─────────────┴─────────────┴─────────────────────┘
```

---

## Phase 1: 플랫폼 기반 + 야추 다이스

### 1주차: 환경 설정 + 인증

#### Day 1-2: 프로젝트 생성

```
□ Firebase 프로젝트 생성
  ├── Firebase Console에서 프로젝트 생성
  ├── Authentication 활성화 (이메일/비밀번호, Google)
  ├── Firestore Database 생성
  ├── Realtime Database 생성
  └── Hosting 설정

□ Supabase 프로젝트 생성
  ├── 프로젝트 생성
  ├── 테이블 생성 (SQL 실행)
  │   ├── game_types
  │   ├── users
  │   ├── user_game_stats
  │   ├── game_logs
  │   └── game_results
  └── API Key 확인

□ Cloudtype 프로젝트 생성
  └── Java 환경 설정
```

#### Day 3-4: Spring Boot 프로젝트 초기화

```
□ Spring Boot 프로젝트 생성
  ├── Spring Initializr (Java 17, Gradle)
  ├── 의존성 추가
  │   ├── spring-boot-starter-web
  │   ├── spring-boot-starter-security
  │   ├── firebase-admin
  │   └── postgresql driver
  └── 기본 구조 생성

□ 설정 파일
  ├── application.yml
  ├── firebase-service-account.json
  └── .env (Supabase 연결 정보)

□ Config 클래스
  ├── FirebaseConfig.java
  ├── SecurityConfig.java
  ├── SupabaseConfig.java
  └── CorsConfig.java
```

#### Day 5-7: React 프로젝트 + 인증

```
□ React 프로젝트 생성
  ├── npm create vite@latest (React + TypeScript)
  ├── 패키지 설치
  │   ├── firebase
  │   ├── react-router-dom
  │   ├── axios
  │   └── tailwindcss (선택)
  └── 폴더 구조 생성

□ Firebase 연동
  ├── firebase.ts (초기화)
  └── useAuth.ts (훅)

□ 인증 구현 (프론트)
  ├── LoginPage.tsx
  ├── RegisterPage.tsx
  └── AuthContext.tsx

□ 인증 구현 (백엔드)
  ├── FirebaseAuthFilter.java (JWT 검증)
  ├── POST /api/auth/register
  └── GET /api/users/me

□ 테스트
  └── 회원가입 → 로그인 → 토큰 검증 흐름 확인
```

---

### 2주차: 로비 + 대기실

#### Day 1-2: 게임 목록 + 로비

```
□ 게임 메타 데이터
  ├── Firestore: games/{gameType} 문서 생성
  └── Supabase: game_types 데이터 INSERT

□ 백엔드 API
  ├── GET /api/games (게임 목록)
  └── GET /api/games/{gameType} (게임 상세)

□ 프론트엔드
  ├── LobbyPage.tsx (게임 선택 화면)
  └── GameSelectCard.tsx (게임 카드 컴포넌트)
```

#### Day 3-4: 방 생성/입장

```
□ 백엔드 API
  ├── GET /api/rooms?gameType=yacht (방 목록)
  ├── POST /api/rooms (방 생성)
  ├── POST /api/rooms/{roomId}/join (입장)
  └── POST /api/rooms/{roomId}/leave (퇴장)

□ Firestore 연동
  ├── rooms/{roomId}/info 문서 생성
  └── FirestoreService.java

□ 프론트엔드
  ├── RoomListPage.tsx (방 목록)
  ├── CreateRoomModal.tsx (방 생성)
  └── useRooms.ts (방 목록 구독)
```

#### Day 5-6: 대기실

```
□ 대기실 기능
  ├── 실시간 플레이어 목록 (onSnapshot)
  ├── 준비 상태 토글
  ├── 게임 시작 (방장)
  └── 방 나가기

□ 백엔드 API
  ├── POST /api/rooms/{roomId}/ready
  └── POST /api/rooms/{roomId}/start

□ 프론트엔드
  ├── WaitingRoom.tsx
  ├── PlayerList.tsx
  └── useRoom.ts
```

#### Day 7: Presence + 채팅

```
□ Presence 구현
  ├── usePresence.ts (Realtime DB)
  ├── 온라인/오프라인 표시
  └── 연결 끊김 알림

□ 채팅 구현
  ├── Firestore: rooms/{roomId}/chat
  ├── ChatBox.tsx
  └── useChatMessages.ts
```

---

### 3주차: 게임 엔진 추상화 + 야추 엔진

#### Day 1-2: 게임 엔진 인터페이스

```
□ 코어 인터페이스/클래스
  ├── GameEngine.java (인터페이스)
  ├── GameState.java (추상 클래스)
  ├── GameAction.java (인터페이스)
  ├── GameResult.java
  ├── ValidationResult.java
  └── GameRegistry.java (엔진 등록/조회)

□ 공통 서비스
  ├── GameDispatcherService.java
  └── POST /api/game/{roomId}/action (통합 엔드포인트)

□ Firestore 서비스
  ├── 게임 상태 읽기/쓰기
  ├── 비공개 상태 읽기/쓰기
  └── 방 상태 업데이트
```

#### Day 3-5: 야추 다이스 엔진

```
□ 야추 모델
  ├── YachtState.java
  ├── YachtAction.java (ROLL_DICE, SCORE)
  ├── YachtScoreboard.java
  └── YachtActionType.java (enum)

□ 야추 엔진
  ├── YachtEngine.java (GameEngine 구현)
  ├── initGame(): 초기 상태 생성
  ├── validateAction(): 검증
  │   ├── 자기 턴인지
  │   ├── 굴림 횟수 체크
  │   └── 카테고리 중복 체크
  ├── applyAction(): 상태 변경
  │   ├── 주사위 굴리기
  │   └── 점수 기록 + 턴 넘기기
  └── checkGameEnd(): 12라운드 종료 체크

□ 점수 계산기
  └── YachtScoreCalculator.java
      ├── Ones ~ Sixes
      ├── Choice
      ├── Four of a Kind
      ├── Full House
      ├── Small Straight
      ├── Large Straight
      └── Yacht
```

#### Day 6-7: 야추 테스트 + 통합

```
□ 단위 테스트
  ├── YachtScoreCalculatorTest.java
  ├── YachtEngineTest.java
  └── 엣지 케이스 테스트

□ 통합 테스트
  ├── 게임 시작 → 상태 확인
  ├── 액션 전송 → Firestore 반영 확인
  └── 게임 종료 → 결과 저장 확인

□ 로그 저장
  └── Supabase game_logs에 이벤트 저장
```

---

### 4주차: 야추 UI + 완성

#### Day 1-3: 야추 게임 UI

```
□ 게임 레이아웃
  ├── YachtGame.tsx (메인)
  ├── GameLayout.tsx (공통 레이아웃)
  └── useYachtGame.ts (상태 구독 + 액션)

□ 주사위 영역
  ├── DiceArea.tsx
  ├── Dice.tsx (개별 주사위)
  ├── 주사위 홀드 토글
  └── 굴리기 버튼 (3회 제한)

□ 점수판
  ├── Scoreboard.tsx
  ├── ScoreRow.tsx
  ├── 카테고리별 점수 표시
  ├── 예상 점수 미리보기
  └── 점수 선택 클릭

□ 플레이어 상태
  ├── PlayerStatus.tsx
  ├── TurnIndicator.tsx
  └── 현재 턴 하이라이트
```

#### Day 4-5: 게임 흐름 완성

```
□ 게임 시작
  └── 대기실 → 게임 화면 전환

□ 턴 진행
  ├── 주사위 굴리기 (최대 3회)
  ├── 홀드 선택
  ├── 점수 카테고리 선택
  └── 다음 플레이어로 턴 넘김

□ 게임 종료
  ├── GameResult.tsx (결과 화면)
  ├── 순위, 점수 표시
  └── 로비로 돌아가기

□ 재접속 처리
  ├── 게임 중 새로고침 → 상태 복구
  └── 연결 끊김 → Presence 표시
```

#### Day 6-7: 테스트 + 배포

```
□ E2E 테스트
  ├── 2인 게임 전체 플레이
  ├── 재접속 테스트
  └── 동시 접속 테스트

□ 배포
  ├── Firebase Hosting 배포 (프론트)
  ├── Cloudtype 배포 (백엔드)
  └── 환경변수 설정

□ 버그 수정 + 폴리싱
  ├── UI/UX 개선
  ├── 에러 핸들링
  └── 로딩 상태
```

---

## Phase 2: 카드 게임 추가

### 5주차: 렉시오 엔진

```
□ 렉시오 규칙 분석
  ├── 카드 구성 (52장)
  ├── 카드 조합 (싱글, 페어, 트리플, 스트레이트, 풀하우스 등)
  ├── 조합 비교 규칙
  └── 승리 조건

□ 렉시오 모델
  ├── LexioState.java
  ├── LexioAction.java (PLAY_CARDS, PASS)
  ├── LexioCard.java
  └── LexioHand.java (조합 타입)

□ 렉시오 엔진
  ├── LexioEngine.java
  ├── 카드 분배 (13장씩)
  ├── 조합 검증
  ├── 조합 비교
  └── 승리 판정

□ 카드 조합 검증기
  └── LexioHandValidator.java
      ├── 싱글, 페어, 트리플
      ├── 스트레이트 (5장)
      ├── 풀하우스
      └── 포카드
```

### 6주차: 렉시오 UI

```
□ 렉시오 게임 UI
  ├── LexioGame.tsx
  ├── useLexioGame.ts
  └── 상태 구독

□ 카드 컴포넌트 (공통)
  ├── Card.tsx
  ├── CardHand.tsx (손패)
  └── 카드 선택 UI

□ 게임 영역
  ├── PlayArea.tsx (낸 카드)
  ├── 조합 표시
  └── 패스 버튼

□ 테스트 + 배포
```

### 7주차: 익스플로딩 키튼 엔진

```
□ 익스플로딩 키튼 규칙 분석
  ├── 카드 종류 (폭탄, 해제, 공격, 스킵 등)
  ├── 카드 효과
  └── 승리 조건 (최후의 1인)

□ 익스플로딩 모델
  ├── ExplodingState.java
  ├── ExplodingAction.java
  └── ExplodingCard.java

□ 익스플로딩 엔진
  ├── ExplodingEngine.java
  ├── 카드 효과 처리
  ├── 폭탄 + 해제 로직
  └── 탈락 처리
```

### 8주차: 익스플로딩 키튼 UI

```
□ 익스플로딩 게임 UI
  ├── ExplodingGame.tsx
  ├── useExplodingGame.ts
  └── 카드 효과 애니메이션

□ 특수 UI
  ├── 덱에서 뽑기
  ├── 폭탄 해제 UI
  ├── 카드 효과 알림
  └── 탈락자 표시

□ 테스트 + 배포
```

---

## Phase 3: 듀얼 게임 추가

### 9-10주차: 7원더스 듀얼

```
□ 7원더스 듀얼 규칙 분석
□ 모델 + 엔진 구현
□ UI 구현
□ 테스트 + 배포
```

### 11-12주차: 스플랜더 듀얼

```
□ 스플랜더 듀얼 규칙 분석
□ 모델 + 엔진 구현
□ UI 구현
□ 테스트 + 배포
```

---

## 작업 체크리스트 (복사용)

### 새 게임 추가 시 체크리스트

```
백엔드:
□ games/{gameName}/ 폴더 생성
□ {Game}State.java
□ {Game}Action.java
□ {Game}Engine.java (GameEngine 구현)
□ 점수/검증 유틸 클래스
□ 단위 테스트
□ 통합 테스트

프론트엔드:
□ features/games/{gameName}/ 폴더 생성
□ {Game}Game.tsx (메인 컴포넌트)
□ use{Game}Game.ts (훅)
□ 게임별 컴포넌트들
□ types/{gameName}.ts (타입 정의)
□ GameRouter.tsx에 case 추가

데이터:
□ Supabase game_types INSERT
□ Firestore games/{gameType} 문서 생성

테스트:
□ 2인 이상 플레이 테스트
□ 재접속 테스트
□ 엣지 케이스 테스트
```

---

## 일일 작업 템플릿

```markdown
## 날짜: YYYY-MM-DD

### 오늘 목표
- [ ] 목표 1
- [ ] 목표 2
- [ ] 목표 3

### 완료
- [x] 완료 항목

### 이슈/블로커
- 이슈 내용

### 내일 계획
- 계획 1
```

---

## 우선순위 정리

```
🔴 P0 (필수 - 게임 안 됨)
├── 인증
├── 방 생성/입장
├── 게임 엔진 코어
├── 게임 상태 동기화
└── 기본 UI

🟡 P1 (중요 - 불편함)
├── 채팅
├── Presence
├── 재접속
├── 통계
└── 턴 타이머

🟢 P2 (있으면 좋음)
├── 소셜 로그인
├── 리더보드
├── 게임 리플레이
└── 사운드/애니메이션
```

---

이 순서대로 하면 돼. 1주차부터 시작할까?