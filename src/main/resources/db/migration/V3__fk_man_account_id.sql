-- Replace the loose man_id VARCHAR duplication on transactions and
-- account_snapshots with a real foreign key to man_accounts.id, so a
-- ManAccount's transactions/snapshots cannot outlive it or point at a
-- nonexistent account, and deleting a ManAccount cascades correctly.

ALTER TABLE transactions ADD COLUMN man_account_id BIGINT;

UPDATE transactions t
    SET man_account_id = ma.id
    FROM man_accounts ma
    WHERE t.man_id = ma.man_id AND t.symbol = ma.symbol;

ALTER TABLE transactions ALTER COLUMN man_account_id SET NOT NULL;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_man_account
    FOREIGN KEY (man_account_id) REFERENCES man_accounts (id) ON DELETE CASCADE;

CREATE INDEX idx_transactions_man_account_id ON transactions (man_account_id);

ALTER TABLE transactions DROP COLUMN man_id;

ALTER TABLE account_snapshots ADD COLUMN man_account_id BIGINT;

UPDATE account_snapshots s
    SET man_account_id = ma.id
    FROM man_accounts ma
    WHERE s.man_id = ma.man_id AND s.symbol = ma.symbol;

ALTER TABLE account_snapshots ALTER COLUMN man_account_id SET NOT NULL;

ALTER TABLE account_snapshots
    ADD CONSTRAINT fk_account_snapshots_man_account
    FOREIGN KEY (man_account_id) REFERENCES man_accounts (id) ON DELETE CASCADE;

CREATE INDEX idx_account_snapshots_man_account_id ON account_snapshots (man_account_id);

ALTER TABLE account_snapshots DROP COLUMN man_id;
