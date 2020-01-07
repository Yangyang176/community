/*回复问题*/
function post() {
    var questionId = $("#question_id").val();
    var content = $("#comment_content").val();
    commentTarget(questionId,1,content);
}
/*回复评论*/
function comment(e) {
    var commentId = e.getAttribute("data-id");
    var content = $("#input-" + commentId).val();
    commentTarget(commentId,2,content);
}
/*根据type回复*/
function commentTarget(targetId,type,content) {
    $.ajax({
        type: "POST",
        url: "/comment",
        contentType:"application/json",
        data: JSON.stringify({
            "parentId":targetId,
            "content":content,
            "type":type
        }),
        success: function (response) {
            if(response.code == 200){
                window.location.reload();
            }else {
                if (response.code == 2003){
                    var isAccepted = confirm(response.message);
                    if (isAccepted){
                        window.open("https://github.com/login/oauth/authorize?client_id=79f540fc58ec56d4fd7d&redirect_uri=http://localhost:8887/callback&scope=user&state=1");
                        window.localStorage.setItem("closable", "true");
                    }
                }else {
                    alert(response.message);
                }
            }
        },
        dataType: "json"
    }); 
}
/*展开二级评论*/
function collapseComment(e) {
    var id = e.getAttribute("data-id");
    var comments = $("#comment-" + id);
    comments.toggleClass("in");
    // e.classList.toggle("active");
    if(comments.hasClass("in")){
        // 显示
        var subCommentContainer = $("#comment-"+id);
        if (subCommentContainer.children().length == 1){
            $.getJSON( "/comment/"+id, function(data) {
                $.each( data.data.reverse(), function(index,comment) {
                    var mediaLeftElement = $("<div/>",{
                        "class":"media-left"
                    }).append($("<img/>",{
                        "class":"media-object img-rounded",
                        "src":comment.user.avatarUrl
                    }));
                    var mediaBodyElement = $("<div/>",{
                        "class":"media-body"
                    }).append($("<h6/>",{
                        "html":comment.user.name
                    })).append($("<div/>",{
                        "html":comment.content
                    })).append($("<div/>",{
                        "class":"menu"
                    }).append($("<span/>",{
                        "class":"pull-right",
                        "html":moment(comment.gmtCreate).format("YYYY-MM-DD")
                    })));
                    var mediaElement = $("<div/>",{
                        "class":"media"
                    }).append(mediaLeftElement).append(mediaBodyElement);
                    var commentElement = $("<div/>",{
                        "class":"col-lg-12 col-md-12 col-sm-12 col-xs-12 comments"
                    }).append(mediaElement);
                    subCommentContainer.prepend(commentElement);
                });
            });
        }
        e.classList.add("active");
    }else {
        // 隐藏
        e.classList.remove("active");
    }
}
// 选择标签
function selectTag(e) {
    var value = e.getAttribute("data-tag");
    var previous = $("#tag").val();
    if (previous.indexOf(value) == -1){
        if (previous){
            $('#tag').val(previous+","+value)
        }else{
            $('#tag').val(value)
        }
    }
}
//显示标签页
function showSelectTag() {
    $("#select-tag").show();
}