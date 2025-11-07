/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
MERGE INTO
    store_returns
        USING(
        SELECT
            ss_ticket_number
        FROM
            store_sales,
            date_dim
        WHERE
            ss_sold_date_sk = d_date_sk
            AND d_date BETWEEN '2002-11-12' AND '2002-11-13'
    ) SOURCE ON
    sr_ticket_number = ss_ticket_number
    WHEN MATCHED THEN DELETE;
