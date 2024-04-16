

select /* TPC-DS query41.tpl 0.62 */  distinct(i_product_name)
from item i1
where i_manufact_id between 744 and 744+40
  and (select count(*) as item_cnt
       from item
       where (i_manufact = i1.i_manufact and
              ((i_category = 'Women' and
                (i_color = 'light' or i_color = 'yellow') and
                (i_units = 'Pallet' or i_units = 'Box') and
                (i_size = 'large' or i_size = 'medium')
                   ) or
               (i_category = 'Women' and
                (i_color = 'firebrick' or i_color = 'cream') and
                (i_units = 'Oz' or i_units = 'Carton') and
                (i_size = 'petite' or i_size = 'extra large')
                   ) or
               (i_category = 'Men' and
                (i_color = 'magenta' or i_color = 'sandy') and
                (i_units = 'Ounce' or i_units = 'Cup') and
                (i_size = 'economy' or i_size = 'N/A')
                   ) or
               (i_category = 'Men' and
                (i_color = 'frosted' or i_color = 'plum') and
                (i_units = 'Tsp' or i_units = 'Bundle') and
                (i_size = 'large' or i_size = 'medium')
                   ))) or
           (i_manufact = i1.i_manufact and
            ((i_category = 'Women' and
              (i_color = 'orchid' or i_color = 'chocolate') and
              (i_units = 'Pound' or i_units = 'Lb') and
              (i_size = 'large' or i_size = 'medium')
                 ) or
             (i_category = 'Women' and
              (i_color = 'slate' or i_color = 'linen') and
              (i_units = 'Gross' or i_units = 'Unknown') and
              (i_size = 'petite' or i_size = 'extra large')
                 ) or
             (i_category = 'Men' and
              (i_color = 'gainsboro' or i_color = 'dim') and
              (i_units = 'Ton' or i_units = 'Dozen') and
              (i_size = 'economy' or i_size = 'N/A')
                 ) or
             (i_category = 'Men' and
              (i_color = 'ivory' or i_color = 'navy') and
              (i_units = 'Tbl' or i_units = 'Bunch') and
              (i_size = 'large' or i_size = 'medium')
                 )))) > 0
order by i_product_name
    limit 100
