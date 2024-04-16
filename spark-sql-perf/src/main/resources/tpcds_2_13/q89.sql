

select /* TPC-DS query89.tpl 0.56 */  *
from(
        select i_category, i_class, i_brand,
               s_store_name, s_company_name,
               d_moy,
               sum(ss_sales_price) sum_sales,
               avg(sum(ss_sales_price)) over
            (partition by i_category, i_brand, s_store_name, s_company_name)
         avg_monthly_sales
        from item, store_sales, date_dim, store
        where ss_item_sk = i_item_sk and
                ss_sold_date_sk = d_date_sk and
                ss_store_sk = s_store_sk and
                d_year in (1999) and
            ((trim(i_category) in ('Shoes','Men','Music') and
              trim(i_class) in ('kids','sports-apparel','classical')
                 )
                or (trim(i_category) in ('Electronics','Women','Jewelry') and
                    trim(i_class) in ('portable','fragrances','diamonds')
                 ))
        group by i_category, i_class, i_brand,
                 s_store_name, s_company_name, d_moy) tmp1
where case when (avg_monthly_sales <> 0) then (abs(sum_sales - avg_monthly_sales) / avg_monthly_sales) else null end > 0.1
order by sum_sales - avg_monthly_sales, s_store_name
    limit 100
