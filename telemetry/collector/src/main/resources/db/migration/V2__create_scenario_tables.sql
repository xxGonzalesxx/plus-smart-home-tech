-- Таблица сценариев
CREATE TABLE IF NOT EXISTS scenarios (
    id BIGSERIAL PRIMARY KEY,
    hub_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(hub_id, name)
);

-- Таблица условий сценариев
CREATE TABLE IF NOT EXISTS scenario_conditions (
    id BIGSERIAL PRIMARY KEY,
    scenario_id BIGINT NOT NULL,
    sensor_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    value INTEGER NOT NULL,
    FOREIGN KEY (scenario_id) REFERENCES scenarios(id) ON DELETE CASCADE
);

-- Таблица действий сценариев
CREATE TABLE IF NOT EXISTS scenario_actions (
    id BIGSERIAL PRIMARY KEY,
    scenario_id BIGINT NOT NULL,
    sensor_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    value INTEGER,
    FOREIGN KEY (scenario_id) REFERENCES scenarios(id) ON DELETE CASCADE
);

-- Индексы для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_scenarios_hub_id ON scenarios(hub_id);
CREATE INDEX IF NOT EXISTS idx_scenarios_enabled ON scenarios(enabled);
CREATE INDEX IF NOT EXISTS idx_scenario_conditions_scenario_id ON scenario_conditions(scenario_id);
CREATE INDEX IF NOT EXISTS idx_scenario_actions_scenario_id ON scenario_actions(scenario_id);
