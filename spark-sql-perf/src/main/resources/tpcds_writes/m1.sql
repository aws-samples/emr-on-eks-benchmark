MERGE INTO web_returns_q1 t
    USING web_returns_file005_match000_notmatch005 s
    ON t.wr_order_number = s.wr_order_number AND t.wr_item_sk = s.wr_item_sk
    WHEN NOT MATCHED THEN INSERT *;