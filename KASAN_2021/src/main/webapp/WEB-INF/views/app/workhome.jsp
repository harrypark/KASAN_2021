<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<body>
<!-- title start -->
<div class="row wrapper border-bottom white-bg">
    <div class="col-lg-9">
        <h2>재택근무</h2>
    </div>
</div>
<!-- title end -->
<div class="wrapper wrapper-content animated fadeInRight">
    <div class="ibox float-e-margins">
       <div class="ibox-title">
           <h5>재택근무</h5>
           <div class="ibox-tools">
               <a class="collapse-link">
                   <i class="fa fa-chevron-up"></i>
               </a>
           </div>
       </div>
       <div class="ibox-content">
       	 <div class="row well">
	       	  <form id="searchParam" name="searchParam">
	       	  <div class="form-group">
                <div class="col-lg-2 col-md-6 col-sm-12">
                	<div class="input-group input-daterange search-daterange">
					    <input type="text" class="form-control trap" id="fromDate" name="fromDate" readonly="readonly"/>
					    <span class="input-group-addon">to</span>
					    <input type="text" class="form-control trap" id="toDate" name="toDate" readonly="readonly">
					</div>
                </div>
                 <div class="col-lg-2 col-md-6 col-sm-12">
                	 <select class="form-control chosen" id="searchDept" name="searchDept">
                        <option value="all">부서_전체</option>
                        <c:forEach items="${deptList}" var="list">
                        	<option value="${list.code }">${list.name }</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-lg-2 col-md-6 col-sm-12">
                    <select class="form-control chosen" id="searchUser" name="searchUser">
                        <option value="all">전체</option>
                    </select>
                </div>
                <div class="col-lg-2 col-md-6 col-sm-12">
					<button class="btn btn-w-m btn-primary m-r-sm" type="button" data-toggle="modal"  data-keyboard="false" data-backdrop="static" data-target="#modal_workhome" style="margin-bottom: 0px;"> Add </button>
                </div>
              </div>
              </form>
          </div>

			<div class="row">
               <!-- dataTable start -->
               <div class="table-responsive">
			        <table id="workhome_table" class="table table-striped table-bordered table-hover" >
			        <thead>
			        <tr>
			        	<th>ID</th>
			        	<th>등록자ID</th>
			            <th>신청일</th>
			            <th>요일</th>
			            <th>시작시간</th>
			            <th>종료시간</th>
			            <th>현지출근</th>
			            <th>현지퇴근</th>
			            <th>등록자</th>
			            <th>등록일</th>
			        </tr>
			        </thead>
			        <tbody>
			        <!-- Data list here -->
			        </tbody>
			        </table>
			  </div>
               <!-- dataTable end -->
   			</div>
        </div>
     </div>

</div>

<!-- Modal (workout) start  -->
<div class="modal inmodal fade modal_workhome" id="modal_workhome" tabindex="-1" role="dialog"  aria-hidden="true"  data-keyboard="false" data-backdrop="static">
	 <div class="modal-dialog">
	    <div class="modal-content">
	        <div class="modal-header">

	            <h4 class="modal-title">재택근무</h4>
	            <p style="font-weight: 300; margin-bottom: 0px;"><i class="fa fa-check-circle-o text-danger"></i> 필수 입력 항목입니다.</p>
	        </div>
	        <form class="form-horizontal" name="workhomeForm" id="workhomeForm" action="">
	        <input type="hidden" id="id" name="id"/>
	        <div class="modal-body">
	        	<div class="form-group"><label class="col-sm-3 control-label">신청일 <i class="fa fa-check-circle-o text-danger"></i></label>
	                <div class="col-sm-9"><input type="text" name="homeDt" id="homeDt" class="form-control work" readonly="readonly"></div>
	            </div>
	            <div class="form-group" id="time-info">
	            	<label class="col-sm-12 text-info" style="text-align: center;">전일 재택근무 : 9시간 이상(점심시간 1시간 포함)</label>
	            	<label class="col-sm-12 text-info" style="text-align: center;">반휴 포함 재택근무 : 4시간 이상(점심시간 제외)</label>
	            </div>
	            <div class="form-group"><label class="col-sm-3 control-label">시작시간 <i class="fa fa-check-circle-o text-danger"></i></label>
	                <div class="col-sm-3">
						<input type="text" class="form-control work startTm" id="startTm" name="startTm"/>
	                </div>
	                <div class="col-sm-3">
	                		<input type="hidden" class="work" id="hereGoYn" name="hereGoYn" value="Y" /><label class="control-label">현지출근</label>
	                </div>
	            </div>
	            <div class="form-group"><label class="col-sm-3 control-label">종료시간 <i class="fa fa-check-circle-o text-danger"></i></label>
	                <div class="col-sm-3">
	                	<input type="text" class="form-control work endTm" id="endTm" name="endTm"/>
	                </div>
	                <div class="col-sm-4">
	                		<input type="hidden" class="work" id="hereOutYn" name="hereOutYn" value="Y" /><label class="control-label">현지퇴근</label>
	                </div>
	            </div>
	            <div class="form-group"><label class="col-sm-3 control-label">메모 </label>
	                <div class="col-sm-9"><textarea id="memo" name="memo" class="form-control work"></textarea></div>
	            </div>
	         </div>

	        <div class="modal-footer">
	        	<a class="btn btn-danger pull-left" id="btnDelete" style="display: none;">Delete</a>
	            <a class="btn btn-white" id="btnCancel">Cancel</a>
	            <a class="btn btn-primary registBt" id="btnSave">Save</a>
	        </div>
			</form>
	    </div>

	</div>
</div>
<!-- Modal (workout) end  -->



<!-- Page-Level Scripts -->
    <script>
    var workhome_table;
    var clickRow;
    //var woAvailStartTm = moment('${wotm.startTm}', 'HH:mm');
    //var woAvailEndTm = moment('${wotm.endTm}', 'HH:mm');
    $(document).ready(function(){

    	workhome_table = $('#workhome_table').dataTable({
    		dom: '<"html5buttons"B>lfTgt<"row"<"col-sm-5"i><"col-sm-7"p>>',
            buttons: [
                { extend: 'copy'},
                //{extend: 'csv'},
                {extend: 'excel', title: '외근공지_'+moment().format('YYYYMMDDHHmmss')},
                //{extend: 'pdf', title: 'ExampleFile'},
                {extend: 'print',
                 customize: function (win){
                        $(win.document.body).addClass('white-bg');
                        $(win.document.body).css('font-size', '10px');
                        $(win.document.body).find('table')
                                .addClass('compact')
                                .css('font-size', 'inherit');
                }
                }
            ],
    	"bFilter" : true
		, "bPaginate": true
		//, "sPaginationType" : "bootstrap_full"
		, "bRetrieve": true
		, "bDeferRender": true
		, "aaSorting": [[ 0, "desc" ]]
   			,"autoWidth": false

		});
    	workhome_table.fnSetColumnVis(0, false);
    	workhome_table.fnSetColumnVis(1, false); //crtd_id

    	//$('div#workhome_table_wrapper div.dataTables_filter').append('<button class="btn btn-w-m btn-primary m-r-sm" type="button" data-toggle="modal"  data-keyboard="false" data-backdrop="static" data-target="#modal_workhome" style="margin-bottom: 0px;"> Add </button>');
    	$('.search-daterange').datepicker({
        	format: 'yyyy-mm-dd',
       		language: "kr",
       		startDate: '2016-08-01',
            keyboardNavigation: false,
            forceParse: false,
            autoclose: true,
            todayHighlight: true
        }).on('hide', function(){
        	workhomeList();
        });
       	$('#fromDate').val(moment().subtract(15, 'days').format('YYYY-MM-DD'));
       	$('#toDate').val(moment().add(15, 'days').format('YYYY-MM-DD'));
       	$('#fromDate').datepicker('setDate', moment().subtract(15, 'days').format('YYYY-MM-DD'));
    	$('#toDate').datepicker('setDate', moment().add(15, 'days').format('YYYY-MM-DD'));

    	workhomeFormReset();

		$('input.startTm, input.endTm').timeAutocomplete({
		    formatter: '24hr',
		    auto_value:false
		});

    	$('#homeDt').datepicker({
    		format: 'yyyy-mm-dd',
    		language: "kr",
    		startDate: 'today',
            keyboardNavigation: false,
            forceParse: false,
            autoclose: true,
            todayHighlight: true
        }).on('hide', function(e) {
        	$(this).blur();
        	if($(this).val()==''){
 	        	$(this).val(moment().format('YYYY-MM-DD'));
 	        }
        });

    	workhomeList();


    	$('#btnCancel').click(function(){
    		workhomeFormReset();
    	});
    	$('div.wrapper-content').on('click','table#workhome_table tbody tr',function(){
    		clickRow = workhome_table.fnGetPosition(this);
    		if( clickRow != null){
    			var rowData = workhome_table.fnGetData(this); // 선택한 데이터 가져오기
    			if(rowData[1] != '${info.id}') return; //자신의 Id가 이니면 Exit

    			if(editPossibleCheck(rowData[2])){
        			$('#modal_workhome .modal-title').text('외근정보 수정');
        			$("#btnSave").removeClass('registBt').addClass('modifyBt').show();
        			$("#btnDelete").show();
        		}else{//수정불가
        			$('#modal_workhome .modal-title').text('외근정보 확인');
        			$('#workhomeForm .work').attr("disabled",true);
        			$("#btnSave").removeClass('registBt').addClass('modifyBt').hide();
        		}
    			//$("#time-info").hide();

    			$.ajax({
    				url : "<c:url value='/app/workhomeDetailByIdAjax'/>",
    				data : {searchId : rowData[0]},
    				type : 'POST',
    				dataType : 'json',
    				success : function(data){
	    					$('#workhomeForm #id').val(data.id);
	    					$('#workhomeForm #homeDt').val(data.homeDt);
	    					$('#workhomeForm #startTm').val(data.startTm);
	    					$('#workhomeForm #endTm').val(data.endTm);
	    					$("#workhomeForm #hereGoYn").prop('checked', data.hereGoYn=='Y'?true:false) ;
	    					$("#workhomeForm #hereOutYn").prop('checked', data.hereOutYn=='Y'?true:false) ;
	    					$('#workhomeForm #memo').val(data.memo);
	    					$('#modal_workhome').modal('show');
    				}
    			});


    		}
    	});


    	$('#btnSave').click(function(e){
    		e.preventDefault();
    		var startTime = moment($('#startTm').val(), 'HH:mm');
    		var endTime = moment($('#endTm').val(), 'HH:mm');

    		/*
    		if(startTime.isBefore(woAvailStartTm) || endTime.isAfter(woAvailEndTm)){
    			alert("외근신청 가능시간을 확인  하세요.");
    			return;
    		}
    		*/

    		if(!startTime.isBefore(endTime)){
    			alert("종료시간은 시작시간 이후로 선택하세요.");
    			return;
    		}



       		if($('#workhomeForm').valid()){
       			if($(this).hasClass('registBt')){
       				registWorkout();
       			}else{
       				editWorkout();
       			}
       		};
       	});

    	function registWorkout(){
    		$.ajax({
   				url: "<c:url value='/app/workhomeInsertAjax'/>",
   				data: $("#workhomeForm").serialize(),
   				type: 'POST',
   				dataType: 'json',
   				beforeSend: function () {
   		        },
   		        complete: function () {
   		        },
   				success: function(data){
   					//fnClickAddRow(data);
   					//workhome_table.fnDraw();
   					//console.log("reg ok")
   					workhomeFormReset();
   					workhomeList();
   				}
   			});
    	}

    	function editWorkout(){
    		if(editPossibleCheck($('#homeDt').val())==false){
    			alert('지난 데이터는 수정할 수 없습니다.');
    			return;
    		}
    		$.ajax({
   				url: "<c:url value='/app/workhomeEditAjax'/>",
   				data: $("#workhomeForm").serialize(),
   				type: 'POST',
   				dataType: 'json',
   				beforeSend: function () {
   		        },
   		        complete: function () {
   		        },
   				success: function(data){
   					//fnClickUpdateRow(data);
   					workhomeList();
   					workhomeFormReset();
   				}
   			});
    	}

    	$('#btnDelete').click(function(e){

   			if(editPossibleCheck($('#homeDt').val())==false){
       			alert('지난 데이터는 삭제할 수 없습니다.');
       			return;
       		}
   			if (confirm("삭제 하시겠습니까?")){
        		$.ajax({
       				url: "<c:url value='/app/workhomeDeleteAjax'/>",
       				data: $("#workhomeForm").serialize(),
       				type: 'POST',
       				dataType: 'json',
       				beforeSend: function () {
       		        },
       		        complete: function () {
       		        },
       				success: function(data){
       					if(data==1){
    	   					//workhome_table.fnDeleteRow(clickRow);
    	   					workhomeList();
    	   					workhomeFormReset();
       					}else{
       						alert("삭제 오류 발생.");
       					}
       				}
       			});
   			}
    	});

    	$("#workhomeForm").validate({
    		rules: {
    			homeDt: { required: true }
     			,startTm: { required: true }
     			,endTm: { required: true }

          }
    	});

    	searchDeptUser();
    	$("#searchDept").change(function(){
    		searchDeptUser('deptChange');
    	})

    	$('#searchUser').change(function(){
    		workhomeList();
    	})




    });

    function searchDeptUser(type){
    	$.ajax({
			url : "<c:url value='/management/getDeptUserAjax'/>",
			data : {searchDept : $('#searchDept').val()},
			type : 'POST',
			dataType : 'json',
			success : function(data){
				$('#searchUser option').remove();
				$('#searchUser').append('<option value="all">전체</option>');
				for(var i=0; i<data.length;i++){
					$('#searchUser').append('<option value="'+data[i].id+'">'+data[i].capsName+'('+data[i].deptName+')</option>');
				}

				if(type == 'deptChange'){
					workhomeList();
				}
			}
		});
    }

      function workhomeList(){
          //console.log('call workhome');
       	$.ajax({
			url: "<c:url value='/app/getUserWorkhomeListAjax'/>",
			data: $("#searchParam").serialize(),
			type: 'POST',
			dataType: 'json',
			beforeSend: function () {
	        },
	        complete: function () {
	        },
			success: function(data){
				workhome_table.fnClearTable();
				for( var i=0; i<data.length; i++){
					fnClickAddRow(data[i]);
				}
				workhome_table.fnDraw();
			}
		});
       }

	function editPossibleCheck(appDate){ //appDate (YYYY-MM-DD)
		var currDate = moment();
		var appDate = moment(appDate,'YYYY-MM-DD');
		var res = currDate.diff(appDate,'days');
		//console.log(res);
		if(res<=0){
			return true;
		}else{
			return false;
		}
	}

    function fnClickAddRow(data){
    	var a = workhome_table.fnAddData( [
					data.id,data.crtdId, data.homeDt,data.weekName, data.startTm, data.endTm, data.hereGoYn,data.hereOutYn,data.crtdNm,data.crtdDt,data.mdfyId,data.mdfyDt
				], false);

    }
    function fnClickUpdateRow(data){
    	workhome_table.fnUpdate( [
					data.id,data.crtdId, data.homeDt,data.weekName, data.startTm, data.endTm, data.hereGoYn,data.hereOutYn,data.crtdNm,data.crtdDt,data.mdfyId,data.mdfyDt
				], clickRow);
    }
    function workhomeFormReset(){
    	$('#modal_workhome .modal-title').text('재택근무 등록');
    	$('#workhomeForm #id').val(0);
		$('#workhomeForm #homeDt').val(moment().format('YYYY-MM-DD'));
 		$('#workhomeForm #startTm').val(moment().format('HH')+':00');
 		$('#workhomeForm #endTm').val(moment().format('HH')+':00');
		$("#workhomeForm #hereGoYn").prop('checked', true);
		$("#workhomeForm #hereOutYn").prop('checked', true) ;
		$('#workhomeForm #memo').val('');
		$('#workhomeForm .work').attr("disabled",false);
		$("#btnDelete").hide();
		$("#time-info").show();
		$("#btnSave").removeClass('modifyBt').addClass('registBt').show();

		$('#modal_workhome em.invalid').hide();
    	$('#modal_workhome .form-control').removeClass('invalid');

    	$("#btnSave").show();

		$('#modal_workhome').modal('hide');
    }
</script>
</body>