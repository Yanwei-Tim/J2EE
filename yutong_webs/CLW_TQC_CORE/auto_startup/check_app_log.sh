#!/bin/bash
# check app log script
# hongy@neusoft.com
# last modified 2010/11/10
#LANG=zh_CN.GBK;export LANG
STATE_OK=0
STATE_WARNING=1
STATE_CRITICAL=2
#############################################################################################
#����˵��: check_app_log.sh �ļ�·�� ��ʽƫ���� �������� ʱ�����ֵ "�ؼ���1" "ERR:�ؼ���2" ... ...
#�ؼ��֣����һ����Ҫ��������������Ĺؼ��֣����ؼ�����","�ָ�
#�����쳣����Ĺؼ��֣���"ERR:"��ͷ
##############################################################################################
verify_int_arg(){
        arg_tmp=`echo $1|sed 's/[0-9]//g'`
        if [ "$arg_tmp" != "" ];then
                echo "�����������->"$1
                exit $STATE_CRITICAL   
        fi
}

transkey(){
        key_tmp=`echo $1|sed "s/,/ /g"`
        for list in $key_tmp
        do
                key_grep=$key_grep"grep \"$list\"|"
        done
        key_grep=`echo $key_grep|sed 's/|$//'`
        echo $key_grep
}

#check_log_time �ļ�·�� ƫ���� ʱ�����ֵ
check_log_time(){
	if [ ! -z $1 -a -s $1 ];then
		real_time=`/usr/bin/date '+%m%d%H%M'`
		log_time=`/usr/bin/tail -1 $1`
		real_hour=${real_time:4:2}
		log_hour=${log_time:11:$2}
		real_minute=${real_time:6:2}
		log_minute=${log_time:14:$2}
		if [ $real_minute -ge $log_minute ];then
			diff_minute=`expr $real_minute - $log_minute`
		else
			diff_minute=`expr $real_minute + 60 - $log_minute`
			real_hour=`expr $real_hour - 1`
		fi
		if [ $real_hour -ge $log_hour ];then
			diff_hour=`expr $real_hour - $log_hour`
		else
			diff_hour=`expr $real_hour + 24 - $log_hour`
		fi
		diff=`expr $diff_hour \* 60 + $diff_minute`
		if [ -z "$diff" ];then
			echo "��־�ļ�$1���ݸ�ʽ�쳣"
			exit $STATE_CRITICAL
		fi
		if [ $diff -gt $3 ];then
			echo "��־�ļ�$1����$diff����û�и���"
			exit $STATE_WARNING
		fi
	else
		echo "��־�ļ�$1�����ڻ�Ϊ��"
		exit $STATE_CRITICAL
	fi
}

#check_key_correct �ļ�·�� �������� �ؼ���
check_key_correct(){
	if [ ! -z "$3" ];then
		key=`transkey $3`
		cmd="/usr/bin/tail -$2 $1|$key"
#		echo "check_correct_exec="$cmd
		ret=`eval $cmd`
		if [ -z "$ret" ];then
			echo "��־�ļ�$1���$2��δ�������ؼ���($3)"
			exit $STATE_WARNING
		fi
	fi
}

check_key_error(){
	if [ ! -z "$3" ];then
		key=`transkey $3`
		cmd="/usr/bin/tail -$2 $1|$key"
#		echo "check_error_exec="$cmd
		ret=`eval $cmd`
		if [ ! -z "$ret" ];then
			echo "��־�ļ�$1���$2�м������ؼ���($3)"
			exit $STATE_CRITICAL
		fi
	fi

}
##############################################################################################
log_file=$1
verify_int_arg $2
offset=$2
verify_int_arg $3
check_lines=$3
verify_int_arg $4
threshold_time_diff=$4

declare -a argv_correct
declare -a argv_error
i=1
j=1
until [ $# -le 4 ]
do
	flag=`echo $5|grep '^ERR:'` 
	if [ -z "$flag" ];then
		argv_correct[$i]=$5
		((i+=1))
		shift
	else
		argv_error[$j]=${5#*ERR:}
		((j+=1))
		shift
	fi
done

check_log_time $log_file $offset $threshold_time_diff

i=1
while [ $i -le ${#argv_correct[@]} ]
do 
	check_key_correct $log_file $check_lines ${argv_correct[$i]}
	((i++))
done
i=1
while [ $i -le ${#argv_error[@]} ]
do
	check_key_error $log_file $check_lines ${argv_error[$i]}
        ((i++))
done

echo "��־�ļ�$log_file����"
exit $STATE_OK

#���./check_app_log.sh  /export/home1/sunxiwei/yt_xiaoche/core/logs/clw-c-info.log 2 500 1 "��������" "DBBuffer"