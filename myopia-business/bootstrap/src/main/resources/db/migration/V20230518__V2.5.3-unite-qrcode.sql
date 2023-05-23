UPDATE m_screening_organization
SET qr_code_config = REPLACE(REPLACE(qr_code_config, '1,', ''), ',1', '');
UPDATE m_screening_organization
SET qr_code_config = 2 where qr_code_config = 1;