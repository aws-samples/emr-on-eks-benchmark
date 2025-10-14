MERGE INTO web_returns_q5 t
        USING web_returns_file005_match000_notmatch050 s
        ON t.wr_order_number = s.wr_order_number AND t.wr_item_sk = s.wr_item_sk
        WHEN NOT MATCHED AND s.wr_item_sk % 2 = 0 THEN INSERT *
        WHEN NOT MATCHED THEN INSERT *;
