CREATE TABLE IF NOT EXISTS scenarios (
     id BIGSERIAL PRIMARY KEY,
     hub_id VARCHAR(255),
    name VARCHAR(255),
    UNIQUE(hub_id, name)
    );

CREATE TABLE IF NOT EXISTS sensors (
    id VARCHAR(255) PRIMARY KEY,
    hub_id VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS conditions (
      id BIGSERIAL PRIMARY KEY,
       type VARCHAR(255),
    operation VARCHAR(255),
    value INTEGER
    );

CREATE TABLE IF NOT EXISTS actions (
     id BIGSERIAL PRIMARY KEY,
     type VARCHAR(255),
    value INTEGER
    );

CREATE TABLE IF NOT EXISTS scenario_conditions (
                                                   scenario_id BIGINT REFERENCES scenarios(id),
    sensor_id VARCHAR(255) REFERENCES sensors(id),
    condition_id BIGINT REFERENCES conditions(id),
    PRIMARY KEY (scenario_id, sensor_id, condition_id)
    );

CREATE TABLE IF NOT EXISTS scenario_actions (
                                                scenario_id BIGINT REFERENCES scenarios(id),
    sensor_id VARCHAR(255) REFERENCES sensors(id),
    action_id BIGINT REFERENCES actions(id),
    PRIMARY KEY (scenario_id, sensor_id, action_id)
    );