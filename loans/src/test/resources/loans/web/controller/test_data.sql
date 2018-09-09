DROP SCHEMA IF EXISTS loans;
CREATE SCHEMA loans;

CREATE TABLE IF NOT EXISTS loan (
  loan_id           DOUBLE       NOT NULL AUTO_INCREMENT,
  amount            DOUBLE       NOT NULL,
  currency          VARCHAR(255),
  email             VARCHAR(255) NOT NULL,
  phone             VARCHAR(255) NOT NULL,
  turnover          DOUBLE,
  term              DOUBLE       NOT NULL,
  status            VARCHAR(255),
  creation_time     DATETIME,
  confirmation_date DATE,
  interest_rate     DOUBLE,
  company_id        INT NOT NULL,
  PRIMARY KEY (loan_id)
);

CREATE TABLE IF NOT EXISTS company (
  company_id DOUBLE NOT NULL,
  name       VARCHAR(255),
  type       VARCHAR(255),
  status     VARCHAR(255),
  PRIMARY KEY (company_id),
  FOREIGN KEY (company_id) REFERENCES loan (loan_id)
);

ALTER TABLE loan SET REFERENTIAL_INTEGRITY FALSE;
ALTER TABLE company SET REFERENTIAL_INTEGRITY FALSE;


INSERT INTO
  company (company_id)
VALUES
  (10),
  (51);

INSERT INTO
  loan (
    loan_id,
    amount,
    currency,
    email,
    phone,
    turnover,
    term,
    company_id,
    status
  )
VALUES
  (
    1, 56000, 'EUR', 'uyo@hj.jj', '622-33-55', 1000000, 5, 10, 'NEW'
  ),
  (
    2, 150000, 'EUR', 'some@tt.jj', '986-09-10', 2000000, 10, 51, 'VALID'
  );
