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
    catalog_returns
        USING(
        SELECT
            cs_order_number
        FROM
            catalog_sales,
            date_dim
        WHERE
            cs_sold_date_sk = d_date_sk
            AND d_date BETWEEN '2002-11-12' AND '2002-11-13'
    ) SOURCE ON
    cr_order_number = cs_order_number
    WHEN MATCHED THEN DELETE;
