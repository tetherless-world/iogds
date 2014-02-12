#3> <#> a <http://purl.org/twc/vocab/conversion/CSV2RDF4LOD_environment_variables> ;
#3>     rdfs:seeAlso 
#3>     <http://purl.org/twc/page/csv2rdf4lod/distributed_env_vars>,
#3>     <https://github.com/timrdf/csv2rdf4lod-automation/wiki/Script:-source-me.sh> .

source /home/iogds/prizms/iogds/data/source/csv2rdf4lod-source-me-for-iogds.sh
source /home/iogds/prizms/iogds/data/source/csv2rdf4lod-source-me-credentials.sh
source /home/iogds/prizms/iogds/data/source/csv2rdf4lod-source-me-on-iogds.sh
export CSV2RDF4LOD_CONVERT_DATA_ROOT="/home/iogds/prizms/iogds/data/source"
export CSV2RDF4LOD_PUBLISH_VARWWW_DUMP_FILES="true"
export CSV2RDF4LOD_PUBLISH_VARWWW_ROOT="/var/www"
export PATH=$PATH`/home/iogds/opt/prizms/bin/install/paths.sh`
export CLASSPATH=$CLASSPATH`/home/iogds/opt/prizms/bin/install/classpaths.sh`
export CSV2RDF4LOD_HOME="/home/iogds/opt/prizms/repos/csv2rdf4lod-automation"
export CSV2RDF4LOD_PUBLISH_VIRTUOSO="true"
export CSV2RDF4LOD_PUBLISH_SUBSET_SAMPLES="true"
export JENAROOT=/home/iogds/opt/apache-jena-2.10.0
export DATAFAQS_HOME="/home/iogds/opt/prizms/repos/DataFAQs"
