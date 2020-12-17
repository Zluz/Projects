$(function() {

	$(window).on("scroll",function(){

        var scrollTop = $(window).scrollTop();

        if(scrollTop>=200){
            $("body").addClass("smaller");
        } else{
            $("body").removeClass("smaller");
        } 

    });

	/*drawer*/
	$(".btn-login").on('click', function(e){
		e.preventDefault();
		$("#drawer").addClass('active');
		$("#drawer-bkg").addClass('active');
	});
	$("#btn-close").on('click', function(e){
		e.preventDefault();
		$("#drawer").removeClass('active');
		$("#drawer-bkg").removeClass('active');
	});
	$("#drawer-bkg").on('click', function(e){
		e.preventDefault();
		$("#drawer").removeClass('active');
		$("#drawer-bkg").removeClass('active');
	});

	/*phone database*/
	$('.btn-show').on('click', function(e){
		e.preventDefault();
		$(this).parent().find('a').toggleClass('visible');
		if($(this).text() == 'SHOW ALL'){
			$(this).text('HIDE ALL');
		}else{
			$(this).text('SHOW ALL');
		}
	});


	$('.span-showme').on('click', function(e){
		e.preventDefault();
		$(this).parent().find('p').toggleClass('visible');
		if($(this).text() == 'more...'){
			$(this).text('hide');
		}else{
			$(this).text('more...');
		}
	});

	$('[data-toggle="tooltip"]').tooltip();
});