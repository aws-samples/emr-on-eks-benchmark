MERGE INTO web_returns_q15 t
        USING web_returns_file050_match001_notmatch0001 s
        ON t.wr_order_number = s.wr_order_number AND t.wr_item_sk = s.wr_item_sk
        WHEN MATCHED THEN UPDATE SET *
        WHEN NOT MATCHED THEN INSERT *;
