MERGE INTO
    inventory
        USING(
        SELECT
            *
        FROM
            (
                SELECT
                    MIN( d_date_sk ) AS min_date
                FROM
                    date_dim
                WHERE
                    d_date BETWEEN '2002-11-14' AND '2002-11-21'
            ) r
        JOIN(
                SELECT
                    MAX( d_date_sk ) AS max_date
                FROM
                    date_dim
                WHERE
                    d_date BETWEEN '2002-11-14' AND '2002-11-21'
            ) s
    ) SOURCE ON
    inv_date_sk >= min_date
    AND inv_date_sk <= max_date
    WHEN MATCHED THEN DELETE;
