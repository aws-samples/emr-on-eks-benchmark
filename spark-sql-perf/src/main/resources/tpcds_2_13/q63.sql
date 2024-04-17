

select /* TPC-DS query63.tpl 0.27 */  *
from (select i_manager_id
           ,sum(ss_sales_price) sum_sales
           ,avg(sum(ss_sales_price)) over (partition by i_manager_id) avg_monthly_sales
      from item
         ,store_sales
         ,date_dim
         ,store
      where ss_item_sk = i_item_sk
        and ss_sold_date_sk = d_date_sk
        and ss_store_sk = s_store_sk
        and d_month_seq in (1223,1223+1,1223+2,1223+3,1223+4,1223+5,1223+6,1223+7,1223+8,1223+9,1223+10,1223+11)
        and ((    trim(i_category) in ('Books','Children','Electronics')
          and trim(i_class) in ('personal','portable','reference','self-help')
          and trim(i_brand) in ('scholaramalgamalg #14','scholaramalgamalg #7',
                          'exportiunivamalg #9','scholaramalgamalg #9'))
          or(    trim(i_category) in ('Women','Music','Men')
              and trim(i_class) in ('accessories','classical','fragrances','pants')
              and trim(i_brand) in ('amalgimporto #1','edu packscholar #1','exportiimporto #1',
                              'importoamalg #1')))
      group by i_manager_id, d_moy) tmp1
where case when avg_monthly_sales > 0 then abs (sum_sales - avg_monthly_sales) / avg_monthly_sales else null end > 0.1
order by i_manager_id
       ,avg_monthly_sales
       ,sum_sales
    limit 100
