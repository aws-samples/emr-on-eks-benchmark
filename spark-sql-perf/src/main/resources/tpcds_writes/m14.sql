MERGE INTO web_returns_q14 t
        USING web_returns_file005_match010_notmatch000 s
        ON t.wr_order_number = s.wr_order_number AND t.wr_item_sk = s.wr_item_sk
        WHEN MATCHED THEN UPDATE SET *
        WHEN NOT MATCHED THEN INSERT *;
