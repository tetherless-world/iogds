#!/bin/bash
#
#3> <> prov:specializationOf <https://github.com/tetherless-world/iogds/tree/master/bin/port-from-logds.sh>;
#3>    rdfs:seeAlso <https://github.com/tetherless-world/iogds/wiki/Porting-from-LOGD> .

SVN_ROOT_URL='https://scm.escience.rpi.edu/svn/public/logd-csv2rdf4lod/data/source'
IOGDS_ROOT='../../../../github/iogds/data/source'

for csv in `ls */catalog/version/*/manual/*csv`; do

   echo
   echo $csv
   # e.g. catalog-opendata-in-th/catalog/version/2012-Jun-05/manual/catalog.opendata.in.th.csv

   version=${csv%/manual/*}; version=${version#*catalog/version/}
   echo "  $version"
   # e.g. 2011-Dec-01

   datasetDir=${csv%version/*}
   pushd $datasetDir &> /dev/null
      latest_version=`cr-list-versions.sh | tail -1`
      echo "  $latest_version"
   popd &> /dev/null

   if [[ "$version" == "$latest_version" && -n "$version" ]]; then
      cached_csv_url="$SVN_ROOT_URL/$csv"
      echo "  cached csv comes from:   $cached_csv_url"

      cached_eparams_url="$SVN_ROOT_URL/$csv.e1.params.ttl"
      echo "  enhancement params comes from: $cached_eparams_url"

      dirname=`dirname $IOGDS_ROOT/$csv`
      global=${dirname%/version/*}
      echo "  global params goes:      $global"

      if [[ ! -e $global/e1.params.ttl ]]; then
         echo "  ln $csv.e1.params.ttl $global/e1.params.ttl"
                 ln $csv.e1.params.ttl $global/e1.params.ttl
      fi
      pushd $global &> /dev/null
         echo "  `pwd`"
         echo "  cr-dcat-retrieval-url.sh -w  $cached_eparams_url"
                 cr-dcat-retrieval-url.sh -w "$cached_eparams_url"
      popd &> /dev/null
   fi
   #mkdir -p $global
done
